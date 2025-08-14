package com.saga.order.client.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    public void send(String topic, String key, Object event, Map<String, String> headers) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            Message<String> msg = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .copyHeaders(headers)
                    .build();

            kafkaTemplate.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message", e);
        }
    }

    @Override
    public void send(String topic, String key, String payload, String headersPayload) {
        try {

            Map<String, String> headersMap = getHeaders(headersPayload);
            Message<String> msg = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .copyHeaders(headersMap)
                    .build();

            kafkaTemplate.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message", e);
        }
    }

    private Map<String, String> getHeaders(String headersPayload) throws JsonProcessingException {
        return objectMapper.readValue(
                headersPayload, new TypeReference<Map<String, String>>() {}
        );
    }
}
