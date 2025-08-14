package com.saga.order.config;

import com.saga.order.model.constant.KafkaHeaders;
import com.saga.order.model.event.in.InventoryReservationCompletedEvent;
import com.saga.order.model.event.in.InventoryReservationFailedEvent;
import com.saga.order.model.event.in.OrderPaymentCompletedEvent;
import com.saga.order.model.event.in.OrderPaymentFailedEvent;
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
    private final KafkaTemplate<String, Object> dlqKafkaTemplate;

    public KafkaListenersConfig(KafkaProperties kafkaProperties,
                                @Qualifier("dlqKafkaTemplate") KafkaTemplate<String, Object> dlqKafkaTemplate) {
        this.kafkaProperties = kafkaProperties;
        this.dlqKafkaTemplate = dlqKafkaTemplate;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentCompletedEvent> orderPaymentCompletedEventContainerFactory() {
        return createFactory(OrderPaymentCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentFailedEvent> orderPaymentFailedEventContainerFactory() {
        return createFactory(OrderPaymentFailedEvent.class);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservationCompletedEvent> inventoryReservationCompletedEventContainerFactory() {
        return createFactory(InventoryReservationCompletedEvent.class);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservationFailedEvent> inventoryReservationFailedEventContainerFactory() {
        return createFactory(InventoryReservationFailedEvent.class);
    }


    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createFactory(Class<T> clazz) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, clazz.getName());

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setCommonErrorHandler(getDefaultErrorHandler());
        factory.setConcurrency(3);
        factory.setRecordInterceptor(mdcRecordInterceptor());
        return factory;
    }

    private DefaultErrorHandler getDefaultErrorHandler() {
        var recoverer = new DeadLetterPublishingRecoverer(
                dlqKafkaTemplate,
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
