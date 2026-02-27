package com.saga.order.client.kafka;

import java.util.Map;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.saga.common.kafkaoutbox.EventPublishException;
import com.saga.common.kafkaoutbox.KafkaSendAck;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class KafkaClientImpl implements KafkaClient {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaClientImpl(@Qualifier("defaultKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }


    @Override
    public CompletableFuture<KafkaSendAck> send(String topic, String key, String payload, String headersPayload) {
        final Message<String> msg;
        try {
            msg = buildMessage(topic, key, payload, headersPayload);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new EventPublishException(topic, key, "Failed to build Kafka message", e));
        }

        return kafkaTemplate.send(msg)
                .handle((sendResult, ex) -> {
                    if (ex != null) {
                        Throwable cause = ex instanceof CompletionException && ex.getCause() != null ? ex.getCause() : ex;
                        throw new EventPublishException(topic, key, "Kafka send failed", cause);
                    }
                    if (sendResult == null || sendResult.getRecordMetadata() == null) {
                        throw new EventPublishException(topic, key, "Kafka send failed: missing metadata", null);
                    }
                    var data = sendResult.getRecordMetadata();
                    return new KafkaSendAck(data.topic(), data.partition(), data.offset(), data.timestamp());
                });
    }

    @Nonnull
    private Message<String> buildMessage(String topic, String key, String payload, String headersPayload) throws JsonProcessingException {
        var headersMap = getHeaders(headersPayload);
        return MessageBuilder
                .withPayload(payload)
                .copyHeaders(headersMap)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .build();
    }

    private Map<String, String> getHeaders(String headersPayload) throws JsonProcessingException {
        if (headersPayload == null || headersPayload.isBlank()) {
            return Collections.emptyMap();
        }
        return objectMapper.readValue(headersPayload, new TypeReference<Map<String, String>>() {
        });
    }
}
