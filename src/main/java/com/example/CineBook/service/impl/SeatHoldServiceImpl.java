package com.example.CineBook.service.impl;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.util.RedisLuaScripts;
import com.example.CineBook.dto.seat.SeatHoldData;
import com.example.CineBook.dto.seat.SeatHoldRequest;
import com.example.CineBook.model.Booking;
import com.example.CineBook.repository.irepository.BookingRepository;
import com.example.CineBook.repository.irepository.TicketRepository;
import com.example.CineBook.service.SeatHoldService;
import com.example.CineBook.websocket.service.SeatWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.CineBook.common.util.RedisLuaScripts.DEBUG_SEAT_SCRIPT;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final SeatWebSocketService seatWebSocketService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final int HOLD_EXPIRATION_MINUTES = 15;
    private static final String SEAT_HOLD_KEY = "seat:hold:%s:%s"; // showtime:seat
    private static final String BOOKING_HOLDS_KEY = "booking:holds:%s"; // bookingId

    @Override
    public void holdSeats(UUID bookingId, SeatHoldRequest request) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new BusinessException(MessageCode.BOOKING_NOT_FOUND);
        }

        UUID seatId = request.getSeatId();
        UUID showtimeId = request.getShowtimeId();

        // Check if seat is already booked (Ticket exists in DB)
        boolean booked = ticketRepository.existsBySeatIdAndShowtimeId(seatId, showtimeId);
        if (booked) {
            throw new BusinessException(MessageCode.SEAT_ALREADY_BOOKED);
        }

        String holdKey = String.format(SEAT_HOLD_KEY, showtimeId, seatId);

        // Hold seat using Redis SETNX
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_EXPIRATION_MINUTES);

        SeatHoldData holdData = SeatHoldData.builder()
                .seatId(seatId)
                .showtimeId(showtimeId)
                .bookingId(bookingId)
                .heldAt(now)
                .expiresAt(expiresAt)
                .build();

        Boolean success = redisTemplate.opsForValue().setIfAbsent(holdKey, holdData, HOLD_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(success)) {
            throw new BusinessException(MessageCode.SEAT_ALREADY_HELD);
        }

        // Track seat in booking's hold set
        String bookingKey = String.format(BOOKING_HOLDS_KEY, bookingId);
        redisTemplate.opsForSet().add(bookingKey, holdKey);
        log.info("Added holdKey {} to bookingKey {}", holdKey, bookingKey);
        log.info("Current holds for booking {}: {}", bookingId, redisTemplate.opsForSet().members(bookingKey));
        redisTemplate.expire(bookingKey, HOLD_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // Broadcast to all clients
        seatWebSocketService.notifySeatSelected(showtimeId, seatId, bookingId, expiresAt);

        log.info("Held seat {} for booking {}", seatId, bookingId);
    }

    // Case SETNX + Java logic, (GET + DELETE)
//    @Override
//    public void releaseSeat(UUID bookingId, UUID showtimeId, UUID seatId) {
//        String holdKey = String.format(SEAT_HOLD_KEY, showtimeId, seatId);
//        SeatHoldData holdData = (SeatHoldData) redisTemplate.opsForValue().get(holdKey);
//
//        // Only release if held by this booking
//        if (holdData != null && holdData.getBookingId().equals(bookingId)) {
//            redisTemplate.delete(holdKey);
//
//            String bookingKey = String.format(BOOKING_HOLDS_KEY, bookingId);
//            redisTemplate.opsForSet().remove(bookingKey, holdKey);
//
//            seatWebSocketService.notifySeatReleased(showtimeId, seatId);
//            log.info("Released seat {} for booking {}", seatId, bookingId);
//        }
//    }

    @Override
    public void releaseSeat(UUID bookingId, UUID showtimeId, UUID seatId) {
        String holdKey = String.format(SEAT_HOLD_KEY, showtimeId, seatId);

        String debugInfo = stringRedisTemplate.execute(
                DEBUG_SEAT_SCRIPT, // Vẫn dùng script debug tôi đưa ở trên
                List.of(holdKey),
                bookingId.toString()
        );

        log.error("RESULT: {}", debugInfo);

        Long deleted = redisTemplate.execute(
                RedisLuaScripts.RELEASE_SEAT_SCRIPT,
                List.of(holdKey),
                bookingId.toString()
        );

        log.info("Lua script returned: {} for seat {} booking {}", deleted, seatId, bookingId);

        if (deleted != null && deleted == -1) {
            log.error("Key not found in Redis: {}", holdKey);
        } else if (deleted != null && deleted == -2) {
            log.error("Failed to decode JSON from Redis");
        } else if (deleted != null && deleted == -3) {
            log.error("bookingId field not found in decoded data");
        } else if (deleted != null && deleted == 0) {
            log.warn("bookingId mismatch. Expected: {}", bookingId);
            SeatHoldData holdData = (SeatHoldData) redisTemplate.opsForValue().get(holdKey);
            log.warn("Stored bookingId: {}", holdData != null ? holdData.getBookingId() : null);
        }

        if (deleted != null && deleted == 1) {
            String bookingKey = String.format(BOOKING_HOLDS_KEY, bookingId);
            redisTemplate.opsForSet().remove(bookingKey, holdKey);

            seatWebSocketService.notifySeatReleased(showtimeId, seatId);
            log.info("Released seat {} for booking {}", seatId, bookingId);
        }
    }

//    @Override
//    public void releaseSeats(UUID bookingId) {
//        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));
//        // Idempotency guard
//        if (booking.getStatus() != BookingStatus.DRAFT) {
//            return;
//        }
//
//        // Update DB first
//        booking.setStatus(BookingStatus.EXPIRED);
//        bookingRepository.save(booking);
//
//        String bookingKey = String.format(BOOKING_HOLDS_KEY, bookingId);
//
//        // SNAPSHOT before LUA run
//        Set<Object> holdKeysSnapshot = redisTemplate.opsForSet().members(bookingKey);
//        Map<String, SeatHoldData> cachedSeats = new HashMap<>();
//        if (holdKeysSnapshot != null) {
//            for (Object holdKeyObj : holdKeysSnapshot) {
//                String holdKey = (String) holdKeyObj;
//                SeatHoldData data = (SeatHoldData) redisTemplate.opsForValue().get(holdKey);
//                if (data != null) {
//                    cachedSeats.put(holdKey, data);
//                }
//            }
//        }

//        if (holdKeys != null) {
//            for (Object holdKeyObj : holdKeys) {
//                String holdKey = (String) holdKeyObj;
//                SeatHoldData holdData = (SeatHoldData) redisTemplate.opsForValue().get(holdKey);
//
//                if (holdData != null) {
//                    redisTemplate.delete(holdKey);
//                    seatWebSocketService.notifySeatReleased(holdData.getShowtimeId(), holdData.getSeatId());
//                }
//            }
//            redisTemplate.delete(bookingKey);
//        }
//        Long released = redisTemplate.execute(
//                RedisLuaScripts.RELEASE_BOOKING_SCRIPT,
//                List.of(bookingKey),
//                bookingId.toString()
//        );
//
//        if (released != null && released > 0) {
//            for (SeatHoldData seat : cachedSeats.values()) {
//                seatWebSocketService.notifySeatReleased(
//                        seat.getShowtimeId(),
//                        seat.getSeatId()
//                );
//            }
//        }
//        log.info("Released {} seats for booking {}", released, bookingId);  // released is number of seat
//    }

    @Override
    public void releaseSeats(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        // Idempotency guard
//        if (booking.getStatus() != BookingStatus.DRAFT) {
//            return;
//        }

        // Update DB first
        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);

        String bookingKey = String.format(BOOKING_HOLDS_KEY, bookingId);
        log.info("Releasing seats for booking key: {}", bookingKey);

        // SNAPSHOT before LUA run (Logic này của bạn OK để phục vụ WebSocket notify)
        Set<Object> holdKeysSnapshot = redisTemplate.opsForSet().members(bookingKey);
        log.info("Snapshot hold keys: {}", holdKeysSnapshot);

        Map<String, SeatHoldData> cachedSeats = new HashMap<>();

        if (holdKeysSnapshot != null) {
            for (Object holdKeyObj : holdKeysSnapshot) {
                log.info("Processing hold key: {}", holdKeyObj);
                String holdKey = (String) holdKeyObj;
                // Lưu ý: Dùng try-catch chỗ này nếu sợ redis lỗi null/format, nhưng cơ bản là ổn
                SeatHoldData data = (SeatHoldData) redisTemplate.opsForValue().get(holdKey);
                if (data != null) {
                    cachedSeats.put(holdKey, data);
                }
            }
        }

        // GỌI SCRIPT ĐÃ FIX
        // inputBookingId sẽ được Lua "tẩy rửa" sạch sẽ nên không sợ dấu " hay khoảng trắng nữa
        Long released = redisTemplate.execute(
                RedisLuaScripts.RELEASE_BOOKING_SCRIPT,
                List.of(bookingKey),
                bookingId.toString()
        );

        log.info("Lua script release result: {}", released);

        if (released != null && released > 0) {
            // Chỉ notify những ghế thực sự release thành công (hoặc notify tất cả trong snapshot cũng được)
            for (SeatHoldData seat : cachedSeats.values()) {
                if (seat != null) {
                    seatWebSocketService.notifySeatReleased(
                            seat.getShowtimeId(),
                            seat.getSeatId()
                    );
                }
            }
        }
        log.info("Released {} seats for booking {}", released, bookingId);
    }

    public List<SeatHoldData> getHoldsByBooking(UUID bookingId) {
        String bookingKey = String.format(BOOKING_HOLDS_KEY, bookingId);
        Set<Object> holdKeys = redisTemplate.opsForSet().members(bookingKey);

        List<SeatHoldData> holds = new ArrayList<>();
        if (holdKeys != null) {
            for (Object holdKeyObj : holdKeys) {
                SeatHoldData data = (SeatHoldData) redisTemplate.opsForValue().get((String) holdKeyObj);
                if (data != null) {
                    holds.add(data);
                }
            }
        }
        return holds;
    }
}
