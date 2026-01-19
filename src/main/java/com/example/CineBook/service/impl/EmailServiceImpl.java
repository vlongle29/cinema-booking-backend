package com.example.CineBook.service.impl;


import com.example.CineBook.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendResetPasswordByEmail(String to, String username, String newResetPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Reset Password - CineBook");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "Your password has been reset.\n" +
                            "New password: %s\n\n" +
                            "Please change your password after login.\n\n" +
                            "Best regards,\n" +
                            "CineBook Team",
                    username, newResetPassword
            ));

            mailSender.send(message);
            log.info("Reset password email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendBookingConfirmationEmail(String to, UUID bookingId, String movieTitle,
                                            LocalDateTime showtimeDate, List<String> seats, BigDecimal totalAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Booking Confirmation - CineBook");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedDate = showtimeDate.format(formatter);
            String seatList = String.join(", ", seats);
            
            message.setText(String.format(
                    "Dear Customer,\n\n" +
                            "Your booking has been confirmed!\n\n" +
                            "Booking ID: %s\n" +
                            "Movie: %s\n" +
                            "Showtime: %s\n" +
                            "Seats: %s\n" +
                            "Total Amount: %,.0f VND\n\n" +
                            "Please arrive 15 minutes before showtime.\n" +
                            "Show this email at the counter to collect your tickets.\n\n" +
                            "Enjoy your movie!\n\n" +
                            "Best regards,\n" +
                            "CineBook Team",
                    bookingId, movieTitle, formattedDate, seatList, totalAmount
            ));

            mailSender.send(message);
            log.info("Booking confirmation email sent to: {} for bookingId: {}", to, bookingId);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to: {}", to, e);
            // Don't throw exception - email failure shouldn't break payment flow
        }
    }

    @Override
    public void sendRefundConfirmationEmail(String to, UUID bookingId, BigDecimal refundAmount, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Refund Confirmation - CineBook");
            
            message.setText(String.format(
                    "Dear Customer,\n\n" +
                            "Your refund request has been processed.\n\n" +
                            "Booking ID: %s\n" +
                            "Refund Amount: %,.0f VND\n" +
                            "Reason: %s\n\n" +
                            "The refund will be credited to your account within 7 business days.\n\n" +
                            "If you have any questions, please contact our support team.\n\n" +
                            "Best regards,\n" +
                            "CineBook Team",
                    bookingId, refundAmount, reason
            ));

            mailSender.send(message);
            log.info("Refund confirmation email sent to: {} for bookingId: {}", to, bookingId);
        } catch (Exception e) {
            log.error("Failed to send refund confirmation email to: {}", to, e);
            // Don't throw exception - email failure shouldn't break refund flow
        }
    }
}
