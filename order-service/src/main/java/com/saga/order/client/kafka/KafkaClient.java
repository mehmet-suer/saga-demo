package com.saga.order.client.kafka;

import com.saga.order.model.dto.KafkaSendAck;

import java.util.concurrent.CompletableFuture;

public interface KafkaClient {
    CompletableFuture<KafkaSendAck> send(String topic, String key, String payload, String headers);
}
