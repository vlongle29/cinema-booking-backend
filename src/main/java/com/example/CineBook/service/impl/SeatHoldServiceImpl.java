package com.example.CineBook.service.impl;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.security.SecurityUtils;
import com.example.CineBook.common.util.RedisLuaScripts;
import com.example.CineBook.dto.seat.*;
import com.example.CineBook.model.Booking;
import com.example.CineBook.model.Customer;
import com.example.CineBook.repository.irepository.BookingRepository;
import com.example.CineBook.repository.irepository.CustomerRepository;
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
import java.util.stream.Collectors;

import static com.example.CineBook.common.util.RedisLuaScripts.DEBUG_SEAT_SCRIPT;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final SeatWebSocketService seatWebSocketService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final int HOLD_EXPIRATION_MINUTES = 15;
    private static final int PRE_HOLD_EXPIRATION_MINUTES = 5;
    private static final String SEAT_HOLD_KEY = "seat:hold:%s:%s"; // showtime:seat
    private static final String BOOKING_HOLDS_KEY = "booking:holds:%s"; // bookingId

    @Override
    public void holdSeats(UUID bookingId, SeatHoldRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));
        
        // Smart handling: If expired, throw specific error for frontend to handle
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException(MessageCode.BOOKING_EXPIRED);
        }
        
        if (booking.getStatus() != BookingStatus.DRAFT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_IN_DRAFT_STATUS);
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

    /**
     * 1	delete thành công (release ghế)
     * 0	bookingId không khớp
     * -1	key không tồn tại
     * -2	JSON decode lỗi
     * -3	không có bookingId
     */
    @Override
    public void releaseSeat(UUID bookingId, UUID showtimeId, UUID seatId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));
        
        // Smart handling: If expired, throw specific error for frontend to handle
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException(MessageCode.BOOKING_EXPIRED);
        }
        
        if (booking.getStatus() != BookingStatus.DRAFT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_IN_DRAFT_STATUS);
        }
        
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

        // Idempotency guard - don't release if already processed
        if (booking.getStatus() != BookingStatus.DRAFT) {
            return;
        }

        // Get holds before clearing for WebSocket notification
        List<SeatHoldData> holds = getHoldsByBooking(bookingId);

        // Update DB first - mark as EXPIRED
        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);

        // Clear holds from Redis
        clearHolds(bookingId);
        
        // Notify WebSocket
        for (SeatHoldData hold : holds) {
            seatWebSocketService.notifySeatReleased(
                    hold.getShowtimeId(),
                    hold.getSeatId()
            );
        }
    }

    @Override
    public void clearHolds(UUID bookingId) {
        String bookingKey = String.format(BOOKING_HOLDS_KEY, bookingId);

        // SNAPSHOT before LUA run
        Set<Object> holdKeysSnapshot = redisTemplate.opsForSet().members(bookingKey);

        // Clear holds from Redis
        // inputBookingId sẽ được Lua "tẩy rửa" sạch sẽ nên không sợ dấu " hay khoảng trắng nữa
        Long released = redisTemplate.execute(
                RedisLuaScripts.RELEASE_BOOKING_SCRIPT,
                List.of(bookingKey),
                bookingId.toString()
        );

        log.info("Lua script clear result: {}", released);
        log.info("Cleared {} holds for booking {}", released, bookingId);
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

    @Override
    public SeatAvailabilityResponse checkAvailability(SeatAvailabilityRequest request) {
        List<UUID> availableSeats = new ArrayList<>();
        List<UUID> unavailableSeats = new ArrayList<>();

        for (UUID seatId : request.getSeatIds()) {
            // Check DB (permanent booking)
            boolean booked = ticketRepository.existsBySeatIdAndShowtimeId(seatId, request.getShowtimeId());
            
            // Check Redis (temporary hold)
            String holdKey = String.format(SEAT_HOLD_KEY, request.getShowtimeId(), seatId);
            boolean held = Boolean.TRUE.equals(redisTemplate.hasKey(holdKey));

            if (booked || held) {
                unavailableSeats.add(seatId);
            } else {
                availableSeats.add(seatId);
            }
        }

        boolean allAvailable = unavailableSeats.isEmpty();
        String message = allAvailable 
            ? "Tất cả ghế đều có sẵn" 
            : String.format("%d/%d ghế không khả dụng", unavailableSeats.size(), request.getSeatIds().size());

        return SeatAvailabilityResponse.builder()
                .allAvailable(allAvailable)
                .availableSeats(availableSeats)
                .unavailableSeats(unavailableSeats)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public SeatPreHoldResponse preHoldSeats(SeatPreHoldRequest request) {
        // Get current user
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID customerId = null;

        if (!SecurityUtils.hasRole("ADMIN")) {
            customerId = customerRepository.findByUserId(userId)
                    .map(Customer::getId)
                    .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));
        }

        // Create temporary draft booking
        Booking tempBooking = Booking.builder()
                .customerId(customerId)
                .showtimeId(request.getShowtimeId())
                .status(BookingStatus.DRAFT)
                .bookingDate(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(PRE_HOLD_EXPIRATION_MINUTES))
                .totalTicketPrice(java.math.BigDecimal.ZERO)
                .totalFoodPrice(java.math.BigDecimal.ZERO)
                .discountAmount(java.math.BigDecimal.ZERO)
                .finalAmount(java.math.BigDecimal.ZERO)
                .build();

        Booking savedBooking = bookingRepository.save(tempBooking);
        UUID tempBookingId = savedBooking.getId();

        List<UUID> successfullyHeld = new ArrayList<>();
        List<UUID> failedSeats = new ArrayList<>();

        // Try to hold each seat
        for (UUID seatId : request.getSeatIds()) {
            try {
                // Check if already booked in DB
                boolean booked = ticketRepository.existsBySeatIdAndShowtimeId(seatId, request.getShowtimeId());
                if (booked) {
                    failedSeats.add(seatId);
                    continue;
                }

                String holdKey = String.format(SEAT_HOLD_KEY, request.getShowtimeId(), seatId);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiresAt = now.plusMinutes(PRE_HOLD_EXPIRATION_MINUTES);

                SeatHoldData holdData = SeatHoldData.builder()
                        .seatId(seatId)
                        .showtimeId(request.getShowtimeId())
                        .bookingId(tempBookingId)
                        .heldAt(now)
                        .expiresAt(expiresAt)
                        .build();

                Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    holdKey, 
                    holdData, 
                    PRE_HOLD_EXPIRATION_MINUTES, 
                    TimeUnit.MINUTES
                );

                if (Boolean.TRUE.equals(success)) {
                    // Track in booking holds
                    String bookingKey = String.format(BOOKING_HOLDS_KEY, tempBookingId);
                    redisTemplate.opsForSet().add(bookingKey, holdKey);
                    redisTemplate.expire(bookingKey, PRE_HOLD_EXPIRATION_MINUTES, TimeUnit.MINUTES);

                    // Broadcast WebSocket
                    seatWebSocketService.notifySeatSelected(
                        request.getShowtimeId(), 
                        seatId, 
                        tempBookingId, 
                        expiresAt
                    );

                    successfullyHeld.add(seatId);
                    log.info("Pre-held seat {} for temp booking {}", seatId, tempBookingId);
                } else {
                    failedSeats.add(seatId);
                }
            } catch (Exception e) {
                log.error("Failed to pre-hold seat {}", seatId, e);
                failedSeats.add(seatId);
            }
        }

        // If no seats were held, delete the temp booking and throw error
        if (successfullyHeld.isEmpty()) {
            bookingRepository.delete(savedBooking);
            throw new BusinessException(MessageCode.SEAT_ALREADY_HELD);
        }

        // If some seats failed, release successful holds and throw error
        if (!failedSeats.isEmpty()) {
            clearHolds(tempBookingId);
            bookingRepository.delete(savedBooking);
            throw new BusinessException(MessageCode.SEAT_ALREADY_HELD);
        }

        return SeatPreHoldResponse.builder()
                .tempBookingId(tempBookingId)
                .showtimeId(request.getShowtimeId())
                .heldSeatIds(successfullyHeld)
                .expiresAt(savedBooking.getExpiredAt())
                .message("Đã giữ ghế tạm thời. Vui lòng hoàn tất thanh toán trong " + PRE_HOLD_EXPIRATION_MINUTES + " phút.")
                .build();
    }

    @Override
    public boolean isSeatHeld(UUID seatId, UUID showtimeId) {
        String holdKey = String.format(SEAT_HOLD_KEY, showtimeId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(holdKey));
    }
}
