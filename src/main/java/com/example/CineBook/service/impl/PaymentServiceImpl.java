package com.example.CineBook.service.impl;

import com.example.CineBook.common.config.VNPAYConfig;
import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.util.RequestUtils;
import com.example.CineBook.common.util.VNPayUtil;
import com.example.CineBook.dto.payment.PaymentResponse;
import com.example.CineBook.dto.payment.QRPaymentResponse;
import com.example.CineBook.dto.payment.RefundResponse;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.EmailService;
import com.example.CineBook.service.PaymentService;
import com.example.CineBook.service.QRCodeService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final VNPAYConfig vnpayConfig;
    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;
    private final SeatWebSocketService seatWebSocketService;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final MovieRepository movieRepository;
    private final SeatRepository seatRepository;
    private final SysUserRepository sysUserRepository;
    private final QRCodeService qrCodeService;
//    private final SeatHoldService seatHoldService;
    private final SeatTypeRepository seatTypeRepository;
//    private final TicketCodeGenerator ticketCodeGenerator;

    @Override
    public PaymentResponse createVNPayPayment(HttpServletRequest request) {
        // 1. Validate input parameters
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.isEmpty()) {
            throw new BusinessException(MessageCode.BAD_REQUEST);
        }

        // 2. Get booking and validate status
        long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");

        Map<String, String> vnpParamsMap = vnpayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_TxnRef", bookingId);
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan booking: " + bookingId);

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", RequestUtils.getClientIp(request));

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
    public QRPaymentResponse createVNPayQRPayment(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_PENDING_PAYMENT);
        }

        // Tạo params cho VNPay
        Map<String, String> vnpParamsMap = vnpayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(booking.getFinalAmount().longValue() * 100));
        vnpParamsMap.put("vnp_TxnRef", bookingId.toString());
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan booking: " + bookingId);

        // Build payment URL
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData);
        String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;

        // Generate QR code từ payment URL
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(paymentUrl);

        return QRPaymentResponse.builder()
                .bookingId(bookingId)
                .qrCodeData(paymentUrl)
                .qrCodeImageBase64(qrCodeBase64)
                .amount(booking.getFinalAmount())
                .transactionRef(bookingId.toString())
                .expiresInSeconds(900) // 15 phút
                .build();
    }

    @Override
    @Transactional
    public void handleVNPayCallback(UUID bookingId, String responseCode, String transactionNo) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        if (!"00".equals(responseCode)) {
            throw new BusinessException(MessageCode.PAYMENT_ERROR);
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_PENDING_PAYMENT);
        }

        log.info("Payment successful for booking {}. VNPay transaction no: {}", bookingId, transactionNo);

        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);

        // Get tickets and notify WebSocket
//        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
//        for (Ticket ticket : tickets) {
//            seatWebSocketService.notifySeatBooked(
//                    ticket.getShowtimeId(),
//                    ticket.getSeatId()
//            );
//        }

        // Send booking confirmation email
        sendBookingConfirmationEmail(booking);
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

        // Send refund confirmation email
        sendRefundConfirmationEmail(booking);

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

    private void sendBookingConfirmationEmail(Booking booking) {
        try {
            Customer customer = customerRepository.findById(booking.getCustomerId()).orElse(null);
            if (customer == null) {
                return;
            }

            SysUser user = sysUserRepository.findById(customer.getUserId()).orElse(null);
            if (user == null || user.getEmail() == null) {
                return;
            }

            Showtime showtime = showtimeRepository.findById(booking.getShowtimeId()).orElse(null);
            Movie movie = showtime != null ? movieRepository.findById(showtime.getMovieId()).orElse(null) : null;

            if (showtime == null || movie == null) {
                log.warn("Cannot send email: showtime or movie not found for booking {}", booking.getId());
                return;
            }

            List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
            List<String> seatNames = tickets.stream()
                    .map(ticket -> {
                        Seat seat = seatRepository.findById(ticket.getSeatId()).orElse(null);
                        return seat != null ? seat.getRowChar() + seat.getSeatNumber() : "Unknown";
                    })
                    .collect(Collectors.toList());

            emailService.sendBookingConfirmationEmail(
                    user.getEmail(),
                    booking.getId(),
                    booking.getBookingCode(),
                    movie.getTitle(),
                    showtime.getStartTime(),
                    seatNames,
                    booking.getFinalAmount()
            );
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for booking {}", booking.getId(), e);
        }
    }

    private void sendRefundConfirmationEmail(Booking booking) {
        try {
            Customer customer = customerRepository.findById(booking.getCustomerId()).orElse(null);
            if (customer == null) {
                log.warn("Cannot send email: customer not found for booking {}", booking.getId());
                return;
            }

            SysUser user = sysUserRepository.findById(customer.getUserId()).orElse(null);
            if (user == null || user.getEmail() == null) {
                log.warn("Cannot send email: user not found or email missing for booking {}", booking.getId());
                return;
            }

            emailService.sendRefundConfirmationEmail(
                    user.getEmail(),
                    booking.getId(),
                    booking.getRefundAmount(),
                    booking.getRefundReason()
            );
        } catch (Exception e) {
            log.error("Failed to send refund confirmation email for booking {}", booking.getId(), e);
        }
    }
}