package com.example.CineBook.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final MessageCode messageCode;

    public BusinessException(MessageCode messageCode) {
        super(messageCode.getMessage());
        this.messageCode = messageCode;
    }
}
