package com.saga.streamer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics")
public record KafkaTopicProperties(
        String orderStatusUpdates,
        String orderPaymentCompleted,
        String orderPaymentFailed,
        String inventoryReservationCompleted,
        String inventoryReservationFailed
) {
}
