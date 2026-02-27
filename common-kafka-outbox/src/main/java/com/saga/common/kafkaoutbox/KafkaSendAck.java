package com.saga.common.kafkaoutbox;

public record KafkaSendAck(String topic,
                           int partition,
                           long offset,
                           long timestamp) {
}
