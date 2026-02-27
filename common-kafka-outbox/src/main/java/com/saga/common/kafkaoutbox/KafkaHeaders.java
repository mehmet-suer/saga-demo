package com.saga.common.kafkaoutbox;

public interface KafkaHeaders {
    String EVENT_ID = "eventId";
    String TRACE_ID = "traceId";
    String TYPE = "type";
}
