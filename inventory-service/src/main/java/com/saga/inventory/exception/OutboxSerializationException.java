package com.saga.inventory.exception;


public class OutboxSerializationException extends RuntimeException {
    public OutboxSerializationException(String msg, Throwable cause) { super(msg, cause); }
}