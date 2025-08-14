package com.saga.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics")
public record KafkaTopicProperties(
        String orderPaymentCompleted,
        String inventoryReservationCompleted,
        String inventoryReservationFailed
) {}