package com.saga.streamer.model.dto;

import java.util.UUID;

public record PaymentStatusEvent(UUID orderId, boolean paid) {}