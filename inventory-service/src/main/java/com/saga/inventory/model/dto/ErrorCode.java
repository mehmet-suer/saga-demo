package com.saga.inventory.model.dto;
public enum ErrorCode {
    UNKNOWN_ERROR,
    VALIDATION_ERROR,
    IDP_CONFLICT;

    public static ErrorCode fromException(Exception ex) {
        return switch (ex) {
            case IllegalArgumentException e -> VALIDATION_ERROR;
            case NullPointerException e -> UNKNOWN_ERROR;
            case IllegalStateException e -> IDP_CONFLICT;
            default -> UNKNOWN_ERROR;
        };
    }
}