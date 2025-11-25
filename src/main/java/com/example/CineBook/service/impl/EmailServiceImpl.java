package com.example.CineBook.service.impl;


import com.example.CineBook.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}
