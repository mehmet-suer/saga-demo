package com.saga.order.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank String userId,
        @NotBlank String productId,
        @NotNull UUID orderId
) {}
