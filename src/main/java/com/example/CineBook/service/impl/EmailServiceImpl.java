package com.example.CineBook.service.impl;


import com.example.CineBook.service.EmailService;
import com.example.CineBook.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
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
    private final QRCodeService qrCodeService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendResetPasswordByEmail(String to, String username, String newResetPassword) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Password - CineBook");
            helper.setText(String.format(
                    "Hello %s,\n\n" +
                            "Your password has been reset.\n" +
                            "New password: %s\n\n" +
                            "Please change your password after login.\n\n" +
                            "Best regards,\n" +
                            "CineBook Team",
                    username, newResetPassword
            ));

            mailSender.send(mimeMessage);
            log.info("Reset password email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendBookingConfirmationEmail(String to, UUID bookingId, String bookingCode, String movieTitle,
                                            LocalDateTime showtimeDate, List<String> seatNames, BigDecimal totalAmount) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Booking Confirmation - CineBook");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedDate = showtimeDate.format(formatter);
            String seatList = String.join(", ", seatNames);
            
            // Generate ONE QR code for the entire booking
            String qrBase64 = qrCodeService.generateQRCodeBase64(bookingCode);
            
            String htmlContent = String.format(
                    "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                    "<div style='background-color: #e50914; padding: 20px; text-align: center;'>" +
                    "<h1 style='color: white; margin: 0;'>CineBook</h1>" +
                    "</div>" +
                    "<div style='padding: 20px; background-color: #f5f5f5;'>" +
                    "<h2 style='color: #e50914;'>Booking Confirmation</h2>" +
                    "<p>Dear Customer,</p>" +
                    "<p>Your booking has been <strong style='color: #28a745;'>confirmed</strong>!</p>" +
                    "<table style='width: 100%%; border-collapse: collapse; margin: 20px 0; background: white;'>" +
                    "<tr style='background-color: #f8f9fa;'><td style='padding: 12px; font-weight: bold; border: 1px solid #dee2e6;'>Booking Code:</td><td style='padding: 12px; border: 1px solid #dee2e6; font-family: monospace; font-size: 16px; color: #e50914;'><strong>%s</strong></td></tr>" +
                    "<tr><td style='padding: 12px; font-weight: bold; border: 1px solid #dee2e6;'>Movie:</td><td style='padding: 12px; border: 1px solid #dee2e6;'>%s</td></tr>" +
                    "<tr style='background-color: #f8f9fa;'><td style='padding: 12px; font-weight: bold; border: 1px solid #dee2e6;'>Showtime:</td><td style='padding: 12px; border: 1px solid #dee2e6;'>%s</td></tr>" +
                    "<tr><td style='padding: 12px; font-weight: bold; border: 1px solid #dee2e6;'>Seats:</td><td style='padding: 12px; border: 1px solid #dee2e6;'>%s</td></tr>" +
                    "<tr style='background-color: #f8f9fa;'><td style='padding: 12px; font-weight: bold; border: 1px solid #dee2e6;'>Number of Tickets:</td><td style='padding: 12px; border: 1px solid #dee2e6;'>%d</td></tr>" +
                    "<tr><td style='padding: 12px; font-weight: bold; border: 1px solid #dee2e6;'>Total Amount:</td><td style='padding: 12px; border: 1px solid #dee2e6; color: #e50914; font-weight: bold;'>%,.0f VND</td></tr>" +
                    "</table>" +
                    "<h3 style='color: #e50914; margin-top: 30px; text-align: center;'>Your E-Ticket QR Code</h3>" +
                    "<div style='text-align: center; background: white; padding: 30px; border-radius: 10px; margin: 20px 0;'>" +
                    "<p style='margin: 0 0 15px 0; font-size: 18px;'><strong>Booking Code: %s</strong></p>" +
                    "<div style='display: inline-block; padding: 20px; border: 3px solid #e50914; border-radius: 10px;'>" +
                    "<img src='data:image/png;base64,%s' alt='QR Code' style='width: 250px; height: 250px; display: block;'/>" +
                    "</div>" +
                    "<p style='margin: 15px 0 0 0; color: #666; font-size: 14px;'>Scan this QR code at the cinema entrance</p>" +
                    "</div>" +
                    "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                    "<p style='margin: 0;'><strong>Important:</strong></p>" +
                    "<ul style='margin: 10px 0; padding-left: 20px;'>" +
                    "<li>Please arrive 15 minutes before showtime</li>" +
                    "<li>Show this QR code at the entrance to check in</li>" +
                    "<li>This QR code is valid for all %d tickets in this booking</li>" +
                    "</ul>" +
                    "</div>" +
                    "<p style='text-align: center; margin-top: 30px; font-size: 18px;'>Enjoy your movie!</p>" +
                    "</div>" +
                    "<div style='background-color: #333; color: #999; padding: 15px; text-align: center; font-size: 12px;'>" +
                    "<p style='margin: 0;'>Best regards,<br/>CineBook Team</p>" +
                    "<p style='margin: 10px 0 0 0;'>© 2026 CineBook. All rights reserved.</p>" +
                    "</div>" +
                    "</body></html>",
                    bookingCode, movieTitle, formattedDate, seatList, seatNames.size(), totalAmount,
                    bookingCode, qrBase64, seatNames.size()
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Booking confirmation email sent to: {} for bookingId: {} with bookingCode: {}", to, bookingId, bookingCode);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to: {}", to, e);
        }
    }

    @Override
    public void sendRefundConfirmationEmail(String to, UUID bookingId, BigDecimal refundAmount, String reason) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Refund Confirmation - CineBook");
            
            String htmlContent = String.format(
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<h2 style='color: #e50914;'>Refund Confirmation</h2>" +
                    "<p>Dear Customer,</p>" +
                    "<p>Your refund request has been processed.</p>" +
                    "<table style='border-collapse: collapse; margin: 20px 0;'>" +
                    "<tr><td style='padding: 8px; font-weight: bold;'>Booking ID:</td><td style='padding: 8px;'>%s</td></tr>" +
                    "<tr><td style='padding: 8px; font-weight: bold;'>Refund Amount:</td><td style='padding: 8px;'>%,.0f VND</td></tr>" +
                    "<tr><td style='padding: 8px; font-weight: bold;'>Reason:</td><td style='padding: 8px;'>%s</td></tr>" +
                    "</table>" +
                    "<p>The refund will be credited to your account within 7 business days.</p>" +
                    "<p>If you have any questions, please contact our support team.</p>" +
                    "<p style='margin-top: 30px; color: #666;'>Best regards,<br/>CineBook Team</p>" +
                    "</body></html>",
                    bookingId, refundAmount, reason
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Refund confirmation email sent to: {} for bookingId: {}", to, bookingId);
        } catch (Exception e) {
            log.error("Failed to send refund confirmation email to: {}", to, e);
        }
    }
}
