package com.saga.payment.client.kafka;

import com.saga.common.kafkaoutbox.KafkaSendAck;

import java.util.concurrent.CompletableFuture;

public interface KafkaClient {
    CompletableFuture<KafkaSendAck> send(String topic, String key, String payload, String headers);
}
