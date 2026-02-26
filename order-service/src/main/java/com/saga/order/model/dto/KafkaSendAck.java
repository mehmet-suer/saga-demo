package com.saga.order.model.dto;

public record KafkaSendAck(String topic,
                           int partition,
                           long offset,
                           long timestamp) {
}
