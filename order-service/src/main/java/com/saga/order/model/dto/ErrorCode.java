package com.saga.order.model.dto;
public enum ErrorCode {
    UNKNOWN_ERROR,
    VALIDATION_ERROR;

    public static ErrorCode fromException(Exception ex) {
        return switch (ex) {
            case IllegalArgumentException e -> VALIDATION_ERROR;
            case NullPointerException e -> UNKNOWN_ERROR;
            default -> UNKNOWN_ERROR;
        };
    }
}