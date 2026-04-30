package com.example.CineBook.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface BookingCodeGenerator {
    String generateBookingCode();
    
    String generateRandomString(int length);
}
