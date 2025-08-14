package com.saga.streamer.model.dto;

import java.util.UUID;

public record InventoryStatusEvent(UUID orderId, boolean reserved) {}