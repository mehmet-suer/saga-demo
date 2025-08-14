package com.saga.order.kafka.producer;

import com.saga.order.client.kafka.KafkaClient;
import com.saga.order.config.KafkaTopicProperties;
import com.saga.order.model.constant.KafkaHeaders;
import com.saga.order.model.event.out.OrderCreatedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderCreatedEventProducer {

    private final KafkaClient kafkaClient;
    private final KafkaTopicProperties kafkaTopicProperties;

    public OrderCreatedEventProducer(KafkaClient kafkaClient, KafkaTopicProperties kafkaTopicProperties) {
        this.kafkaClient = kafkaClient;
        this.kafkaTopicProperties = kafkaTopicProperties;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaClient.send(
                kafkaTopicProperties.orderCreated(),
                event.orderId().toString(),
                event,
                Map.of(KafkaHeaders.EVENT_ID, event.eventId().toString(), KafkaHeaders.TRACE_ID, event.traceId().toString())
        );
    }
}