package com.example.CineBook.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EmailService {
    /**
     * Send reset password by email
     * @param to
     * @param username
     * @param newResetPassword
     */
    void sendResetPasswordByEmail(String to, String username, String newResetPassword);
    
    /**
     * Send booking confirmation email after successful payment
     * @param to Customer email
     * @param bookingId Booking ID
     * @param movieTitle Movie title
     * @param showtimeDate Showtime date and time
     * @param seats List of seat numbers
     * @param totalAmount Total amount paid
     */
    void sendBookingConfirmationEmail(String to, UUID bookingId, String movieTitle, 
                                     LocalDateTime showtimeDate, List<String> seats, BigDecimal totalAmount);
    
    /**
     * Send refund confirmation email
     * @param to Customer email
     * @param bookingId Booking ID
     * @param refundAmount Refund amount
     * @param reason Refund reason
     */
    void sendRefundConfirmationEmail(String to, UUID bookingId, BigDecimal refundAmount, String reason);
}
