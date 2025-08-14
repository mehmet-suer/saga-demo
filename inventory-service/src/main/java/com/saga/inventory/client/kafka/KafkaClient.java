package com.saga.inventory.client.kafka;

public interface KafkaClient {
    void send(String topic, String key, Object event);
    void send(String topic, String key, String payload, String headers);

}
