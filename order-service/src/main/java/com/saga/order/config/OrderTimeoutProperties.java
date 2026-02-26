package com.saga.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.timeout")
public record OrderTimeoutProperties(long minutes) {
    public OrderTimeoutProperties {
        if (minutes <= 0) {
            minutes = 10;
        }
    }
}
