package com.example.CineBook.service.impl;

import com.example.CineBook.service.BookingCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BookingCodeGeneratorServiceImpl implements BookingCodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Generate unique booking code
     * Format: BKG-YYYYMMDD-XXXXX
     * Example: BKG-20260120-A3B9C
     */
    public String generateBookingCode() {
        String date = LocalDateTime.now().format(formatter);
        String randomPart = generateRandomString(5);
        return String.format("BKG-%s-%s", date, randomPart);
    }

    public String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
