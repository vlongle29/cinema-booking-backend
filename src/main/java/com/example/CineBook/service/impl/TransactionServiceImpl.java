package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.constant.TransactionStatus;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.transaction.TransactionRequest;
import com.example.CineBook.dto.transaction.TransactionResponse;
import com.example.CineBook.mapper.TransactionMapper;
import com.example.CineBook.model.Booking;
import com.example.CineBook.model.Transaction;
import com.example.CineBook.repository.irepository.BookingRepository;
import com.example.CineBook.repository.irepository.TransactionRepository;
import com.example.CineBook.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionResponse processPayment(TransactionRequest request) {
        // Kiểm tra booking tồn tại
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        // Kiểm tra booking đã thanh toán chưa
        if (transactionRepository.existsByBookingId(request.getBookingId())) {
            throw new BusinessException(MessageCode.BOOKING_ALREADY_PAID);
        }

        // Kiểm tra trạng thái booking
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_PENDING_PAYMENT);
        }

        // Kiểm tra booking hết hạn
        if (booking.getExpiredAt() != null && booking.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageCode.BOOKING_EXPIRED);
        }

        // Giả lập thanh toán (fake payment)
        boolean paymentSuccess = simulatePayment();

        Transaction transaction = Transaction.builder()
                .bookingId(request.getBookingId())
                .transactionCode(generateTransactionCode())
                .paymentGateway(request.getPaymentGateway() != null ? request.getPaymentGateway() : "FAKE_GATEWAY")
                .amount(booking.getFinalAmount())
                .status(paymentSuccess ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                .transactionTime(LocalDateTime.now())
                .rawResponseData("{\"status\":\"" + (paymentSuccess ? "success" : "failed") + "\"}")
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // Nếu thanh toán thành công, cập nhật booking status
        if (paymentSuccess) {
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);
        } else {
            throw new BusinessException(MessageCode.PAYMENT_FAILED);
        }

        return transactionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getByBookingId(UUID bookingId) {
        Transaction transaction = transactionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.TRANSACTION_NOT_FOUND));
        return transactionMapper.toResponse(transaction);
    }

    private boolean simulatePayment() {
        // Giả lập thanh toán luôn thành công
        return true;
    }

    private String generateTransactionCode() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
