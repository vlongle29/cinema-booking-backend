package com.example.CineBook.common.util;


import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisLuaScripts {

    // holdKey: String
    // bookingId: UUID

    // Trước khi lưu một object vào Redis thì Spring đã serialize object sang JSON bằng Jackson.
    // Khi đọc lại object từ Redis, Lua script cần decode JSON để lấy ra trường bookingId để so sánh.
    // Lưu ý: UUID trong JSON được serialize thành mảng ["java.util.UUID", "uuid-string"]
    // Example:
    // [
    //  "com.example.SeatHoldData",
    //  {
    //    "seatId": "28b79835-0ef9-4063-a590-e18d5f326527",
    //    "showtimeId": "0cd15241-840a-4e5b-b52b-669f313834b7",
    //    "bookingId": "b4036749-5766-4987-a736-3fb2a6b035b2",
    //    "heldAt": "2026-01-05T13:51:18.650497700",
    //    "expiresAt": "2026-01-05T14:06:18.650497700"
    //  }
    //]

    public static final RedisScript<Long> RELEASE_SEAT_SCRIPT = RedisScript.of(
            """
                    local holdKey = KEYS[1]
                    local inputId = ARGV[1]
                    
                    local value = redis.call("GET", holdKey)
                    if not value then return -1 end
                    
                    local ok, decoded = pcall(cjson.decode, value)
                    if not ok then return -2 end
                    
                    -- Xử lý trường hợp dữ liệu bị bọc (nếu có)
                    local data = decoded
                    if type(decoded) == "table" and decoded[2] and type(decoded[1]) == "string" then
                        data = decoded[2]
                    end
                    
                    if not data.bookingId then return -3 end
                    
                    local storedId = tostring(data.bookingId)
                    local inputStr = tostring(inputId)
                    
                    -- QUAN TRỌNG: Loại bỏ cả khoảng trắng (%s) VÀ dấu ngoặc kép (") 
                    -- để tránh lỗi do Jackson Serialize thêm dấu ngoặc vào Input
                    local cleanStored = string.gsub(storedId, '["%s]', '')
                    local cleanInput = string.gsub(inputStr, '["%s]', '')
                    
                    -- So sánh chữ thường
                    if string.lower(cleanStored) == string.lower(cleanInput) then
                        return redis.call("DEL", holdKey)
                    end
                    
                    return 0
                    """,
            Long.class
    );

    public static final RedisScript<String> DEBUG_SEAT_SCRIPT = RedisScript.of(
            """
                    local holdKey = KEYS[1]
                    local inputId = ARGV[1]
                    
                    local value = redis.call("GET", holdKey)
                    if not value then return cjson.encode("ERROR: Key not found in Redis") end
                    
                    local ok, decoded = pcall(cjson.decode, value)
                    if not ok then return cjson.encode("ERROR: Cannot decode JSON value in Redis") end
                    
                    local storedRaw = decoded.bookingId
                    
                    if storedRaw == nil then
                        return cjson.encode("ERROR: bookingId field is NIL. Full JSON: " .. value)
                    end
                    
                    local typeOfStored = type(storedRaw)
                    local contentOfStored = ""
                    
                    if typeOfStored == "table" then
                        contentOfStored = cjson.encode(storedRaw)
                    else
                        contentOfStored = tostring(storedRaw)
                    end
                    
                    local report = string.format(
                        "DEBUG INFO: Input='%s' | Stored='%s' | LuaType=%s | LuaValue=%s", 
                        inputId, 
                        value, 
                        typeOfStored, 
                        contentOfStored
                    )
                    
                    -- QUAN TRỌNG: Encode thành JSON string để Jackson không bị lỗi
                    return cjson.encode(report)
                    """,
            String.class
    );


    public static final RedisScript<Long> RELEASE_BOOKING_SCRIPT = RedisScript.of(
            """
            local bookingKey = KEYS[1]
            local inputBookingId = ARGV[1]
            
            -- 1. CLEAN INPUT BOOKING_ID (Xóa ngoặc kép và khoảng trắng)
            local cleanInput = string.gsub(tostring(inputBookingId), '["%s]', '')
            cleanInput = string.lower(cleanInput)
            
            local holdKeys = redis.call("SMEMBERS", bookingKey)
            local releasedCount = 0
            
            for _, rawHoldKey in ipairs(holdKeys) do
                -- 2. FIX QUAN TRỌNG: CLEAN KEY LẤY TỪ SET
                -- Key trong Set bị dính ngoặc kép (") do Serializer, cần gỡ ra mới GET được
                local holdKey = string.gsub(rawHoldKey, '["%s]', '')
                
                local value = redis.call("GET", holdKey)
                
                if value then
                    local ok, decoded = pcall(cjson.decode, value)
                    if ok and decoded then
                        
                        -- Xử lý wrapper Jackson (nếu có)
                        local data = decoded
                        if type(decoded) == "table" and decoded[2] and type(decoded[1]) == "string" then
                            data = decoded[2]
                        end
                
                        if data.bookingId then
                            -- 3. CLEAN STORED BOOKING_ID
                            local storedBookingId = tostring(data.bookingId)
                            local cleanStored = string.gsub(storedBookingId, '["%s]', '')
                            cleanStored = string.lower(cleanStored)
                
                            -- 4. SO SÁNH
                            if cleanStored == cleanInput then
                                redis.call("DEL", holdKey)
                                releasedCount = releasedCount + 1
                            end
                        end
                    end
                end
            end
            
            -- Xóa luôn Set booking
            redis.call("DEL", bookingKey)
            
            return releasedCount
            """,
            Long.class
    );
}
/**
 * TIP LÀM VIỆC VỚI LUA SCRIPT
 * <p>
 * Lý do cái "siêu hạt nhân" đó chạy được là vì nó có thêm đoạn code này: string.gsub(..., '["%s]', '') (Xóa cả khoảng trắng lẫn dấu ngoặc kép).
 * 1. Ở phía Java: Khi bạn truyền bookingId.toString() vào ARGV[1] thông qua RedisTemplate (vốn đang dùng Jackson Serializer), nó đã biến chuỗi
 * uuid thành một chuỗi JSON có ngoặc kép: "b403..." (độ dài 38 ký tự).
 * <p>
 * 2. Ở phía Redis (Lua): Khi cjson.decode giải mã object, nó lấy ra bookingId là chuỗi trần: b403... (độ dài 36 ký tự).
 * <p>
 * 3. Hệ quả: Code cũ chỉ xóa khoảng trắng, nên "b403..." (Input) $\neq$ b403... (Stored).
 * <p>
 * 4. Giải pháp: Script "hạt nhân" đã thẳng tay lột bỏ mọi dấu ngoặc kép thừa, đưa cả 2 về dạng nguyên thủy nên mới khớp nhau.
 */
