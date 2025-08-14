package com.saga.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics")
public record KafkaTopicProperties(
        String orderCreated,
        String orderPaymentCompleted,
        String orderPaymentFailed,
        String inventoryReservationCompleted,
        String inventoryReservationFailed
) {
}
