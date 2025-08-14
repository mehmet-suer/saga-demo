package com.saga.streamer.config;

import com.saga.streamer.model.dto.InventoryStatusEvent;
import com.saga.streamer.model.dto.PaymentStatusEvent;
import com.saga.streamer.model.event.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
public class SerdeConfig {


    @Bean
    public JsonSerde<InventoryReservationCompletedEvent> inventoryReservationCompletedEventSerde() {
        return createSerde(InventoryReservationCompletedEvent.class);
    }

    @Bean
    public JsonSerde<InventoryReservationFailedEvent> inventoryReservationFailedEventSerde() {
        return createSerde(InventoryReservationFailedEvent.class);
    }

    @Bean
    public JsonSerde<OrderPaymentCompletedEvent> orderPaymentCompletedSerde() {
        return createSerde(OrderPaymentCompletedEvent.class);
    }

    @Bean
    public JsonSerde<OrderPaymentFailedEvent> orderPaymentFailedEventSerde() {
        return createSerde(OrderPaymentFailedEvent.class);
    }

    @Bean
    public JsonSerde<OrderStatus> orderStatusSerde() {
        return createSerde(OrderStatus.class);
    }

    @Bean
    public JsonSerde<InventoryStatusEvent> inventoryStatusSerde() {
        return createSerde(InventoryStatusEvent.class);
    }

    @Bean
    public JsonSerde<PaymentStatusEvent> paymentStatusSerde() {
        return createSerde(PaymentStatusEvent.class);
    }

    private <T> JsonSerde<T> createSerde(Class<T> clazz) {
        JsonSerde<T> serde = new JsonSerde<>(clazz);
        serde.deserializer().setUseTypeHeaders(false);
        return serde;
    }
}
