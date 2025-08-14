package com.saga.payment.config;

import com.saga.payment.model.constant.KafkaHeaders;
import com.saga.payment.model.event.in.InventoryReservationFailedEvent;
import com.saga.payment.model.event.in.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaListenersConfig {
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaListenersConfig(KafkaProperties kafkaProperties,
                                @Qualifier("dlqKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedEventConsumerFactory() {
        return createFactory(OrderCreatedEvent.class);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createFactory(Class<T> clazz) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, clazz.getName());

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setCommonErrorHandler(getDefaultErrorHandler());
        factory.setRecordInterceptor(mdcRecordInterceptor());
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservationFailedEvent> inventoryReservationFailedEventContainerFactory() {
        return createFactory(InventoryReservationFailedEvent.class);
    }

    private DefaultErrorHandler getDefaultErrorHandler() {
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (r, e) -> new TopicPartition(r.topic() + ".DLQ", r.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
    }


    private <T> RecordInterceptor<String, T> mdcRecordInterceptor() {
        return new RecordInterceptor<>() {
            @Override
            public ConsumerRecord<String, T> intercept(ConsumerRecord<String, T> record,
                                                       Consumer<String, T> consumer) {
                var traceHeader = record.headers().lastHeader(KafkaHeaders.TRACE_ID);
                var eventHeader = record.headers().lastHeader(KafkaHeaders.EVENT_ID);
                MDC.put(KafkaHeaders.TYPE, "EVENT");

                if (traceHeader != null) {
                    MDC.put(KafkaHeaders.TRACE_ID, new String(traceHeader.value(), StandardCharsets.UTF_8));
                }
                if (eventHeader != null) {
                    MDC.put(KafkaHeaders.EVENT_ID, new String(eventHeader.value(), StandardCharsets.UTF_8));
                }
                return record;
            }

            @Override
            public void afterRecord(ConsumerRecord<String, T> record, Consumer<String, T> consumer) {
                MDC.clear();
            }
        };
    }
}
