package com.example.CineBook.service;

import java.util.List;

public interface EmailService {
    /**
     * Send reset password by email
     * @param to
     * @param username
     * @param newResetPassword
     */
    void sendResetPasswordByEmail(String to, String username, String newResetPassword);
}
