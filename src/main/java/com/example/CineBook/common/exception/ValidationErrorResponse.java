package com.example.CineBook.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse  {
    private final Map<String, String> errors;

    public ValidationErrorResponse(int status, String message, String code,
                                    Map<String, String> errors) {
        super(status, message, code);
        this.errors = errors;
    }
}
