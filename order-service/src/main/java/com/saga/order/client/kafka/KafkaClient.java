package com.saga.order.client.kafka;

import java.util.Map;

public interface KafkaClient {
    void send(String topic, String key, Object event, Map<String, String> headers);
    void send(String topic, String key, String payload, String headers);
}
