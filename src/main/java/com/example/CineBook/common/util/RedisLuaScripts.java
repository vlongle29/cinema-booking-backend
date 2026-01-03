package com.example.CineBook.common.util;


import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisLuaScripts {

    public static final RedisScript<Long> RELEASE_SEAT_SCRIPT =
            RedisScript.of(
                    """
                            local value = redis.call("GET", KEYS[1])
                            if not value then
                                return 0
                            end
                            local data = cjson.decode(value)
                            if data.bookingId == ARGV[1] then
                                return redis.call("DEL", KEYS[1])
                            end
                            return 0
                            """,
                    Long.class
            );

    public static final RedisScript<Long> RELEASE_BOOKING_SCRIPT =
            RedisScript.of(
                    """
                            local bookingKey = KEYS[1]
                            local bookingId = ARGV[1]
                            
                            local holdKeys = redis.call("SMEMBERS", bookingKey)
                            local releasedCount = 0
                            
                            for _, holdKey in ipairs(holdKeys) do
                                local value = redis.call("GET", holdKey)
                                if value then
                                    local data = cjson.decode(value)
                                    if data.bookingId == bookingId then
                                        redis.call("DEL", holdKey)
                                        releasedCount = releasedCount + 1
                                    end
                                end
                            end
                            redis.call("DEL", bookingKey)
                            return releasedCount
                            """,
                    Long.class
            );


}
