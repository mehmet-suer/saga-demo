package com.saga.payment.client.kafka;

import java.util.Map;

public interface KafkaClient {
    void send(String topic, String key, Object event);
    void send(String topic, String key, String payload, String headers);
}
