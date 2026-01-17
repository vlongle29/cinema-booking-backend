package com.example.CineBook.service.impl;

import com.example.CineBook.common.config.VNPAYConfig;
import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.util.RequestUtils;
import com.example.CineBook.common.util.VNPayUtil;
import com.example.CineBook.dto.payment.PaymentResponse;
import com.example.CineBook.dto.payment.RefundResponse;
import com.example.CineBook.model.Booking;
import com.example.CineBook.model.Showtime;
import com.example.CineBook.model.Ticket;
import com.example.CineBook.repository.irepository.BookingRepository;
import com.example.CineBook.repository.irepository.ShowtimeRepository;
import com.example.CineBook.repository.irepository.TicketRepository;
import com.example.CineBook.service.PaymentService;
import com.example.CineBook.websocket.service.SeatWebSocketService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final VNPAYConfig vnpayConfig;
    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;
    private final SeatWebSocketService seatWebSocketService;

    @Override
    public PaymentResponse createVNPayPayment(HttpServletRequest request) {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.isEmpty()) {
            throw new BusinessException(MessageCode.BAD_REQUEST);
        }
        
        long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");
        
        Map<String, String> vnpParamsMap = vnpayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_TxnRef", bookingId); // Use bookingId as transaction reference
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan booking: " + bookingId);
        
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", RequestUtils.getClientIp(request));
        
        // build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    @Transactional
    public void handleVNPayCallback(UUID bookingId, String responseCode, String transactionNo) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));
        log.info("Handling VNPay callback for bookingId={}, responseCode={}, transactionNo={}",
                bookingId, responseCode, transactionNo);
        if (!"00".equals(responseCode)) {
            throw new BusinessException(MessageCode.PAYMENT_ERROR);
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_PENDING_PAYMENT);
        }

        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public RefundResponse refundBooking(UUID bookingId, String reason) {
        // 1. Get booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        // 2. Validate: Can only refund PAID bookings
        if (booking.getStatus() != BookingStatus.PAID) {
            throw new BusinessException(MessageCode.BOOKING_NOT_PAID);
        }

        // 3. Check if already refunded
        if (booking.getStatus() == BookingStatus.REFUNDED) {
            throw new BusinessException(MessageCode.BOOKING_ALREADY_REFUNDED);
        }

        // 4. Get showtime to check if it has started
        Showtime showtime = showtimeRepository.findById(booking.getShowtimeId())
                .orElseThrow(() -> new BusinessException(MessageCode.SHOWTIME_NOT_FOUND));

        // 5. Check refund policy: Must cancel 24h before showtime
        if (showtime.getStartTime().minusHours(24).isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageCode.REFUND_TOO_LATE);
        }

        // 6. Update booking status
        booking.setStatus(BookingStatus.REFUNDED);
        booking.setRefundAmount(booking.getFinalAmount());
        booking.setRefundDate(LocalDateTime.now());
        booking.setRefundReason(reason);
        booking.setRefundTransactionNo("REFUND_" + UUID.randomUUID().toString().substring(0, 8));
        bookingRepository.save(booking);

        // 7. Release seats back to available
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        for (Ticket ticket : tickets) {
            seatWebSocketService.notifySeatReleased(
                    ticket.getShowtimeId(),
                    ticket.getSeatId()
            );
        }

        log.info("Refunded booking {} with amount {}", bookingId, booking.getFinalAmount());

        // 8. Return response
        return RefundResponse.builder()
                .bookingId(bookingId)
                .refundAmount(booking.getFinalAmount())
                .status("SUCCESS")
                .message("Refund processed successfully. Money will be returned within 7 business days.")
                .refundDate(booking.getRefundDate())
                .transactionNo(booking.getRefundTransactionNo())
                .build();
    }
}