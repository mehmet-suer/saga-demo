package com.saga.inventory.exception;


import com.saga.inventory.model.dto.ErrorCode;
import com.saga.inventory.model.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                ex.getMessage() != null ? ex.getMessage() : "Unexpected error occurred",
                ErrorCode.fromException(ex),
                Instant.now()
        );

        return ResponseEntity.internalServerError().body(response);
    }
}
