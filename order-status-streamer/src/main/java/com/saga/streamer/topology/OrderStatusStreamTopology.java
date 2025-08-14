package com.saga.streamer.topology;

import com.saga.streamer.config.KafkaTopicProperties;
import com.saga.streamer.model.dto.InventoryStatusEvent;
import com.saga.streamer.model.dto.PaymentStatusEvent;
import com.saga.streamer.model.event.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
public class OrderStatusStreamTopology {

    private final KafkaTopicProperties kafkaTopicProperties;

    public OrderStatusStreamTopology(KafkaTopicProperties kafkaTopicProperties) {
        this.kafkaTopicProperties = kafkaTopicProperties;
    }

    @Bean
    public KStream<String, OrderStatus> orderStatusStream(
            StreamsBuilder builder,
            JsonSerde<InventoryReservationCompletedEvent> inventoryReservationCompletedEventSerde,
            JsonSerde<InventoryReservationFailedEvent> inventoryReservationFailedEventSerde,
            JsonSerde<OrderPaymentCompletedEvent> orderPaymentCompletedSerde,
            JsonSerde<OrderPaymentFailedEvent> orderPaymentFailedEventSerde,
            JsonSerde<OrderStatus> orderStatusSerde
    ) {
        KStream<String, OrderPaymentCompletedEvent> paymentCompletedStream =
                builder.stream(kafkaTopicProperties.orderPaymentCompleted(), Consumed.with(Serdes.String(), orderPaymentCompletedSerde));

        KStream<String, OrderPaymentFailedEvent> paymentFailedStream =
                builder.stream(kafkaTopicProperties.orderPaymentFailed(), Consumed.with(Serdes.String(), orderPaymentFailedEventSerde));


        KStream<String, InventoryReservationCompletedEvent> inventoryCompletedStream =
                builder.stream(kafkaTopicProperties.inventoryReservationCompleted(), Consumed.with(Serdes.String(), inventoryReservationCompletedEventSerde));

        KStream<String, InventoryReservationFailedEvent> inventoryFailedStream =
                builder.stream(kafkaTopicProperties.inventoryReservationFailed(), Consumed.with(Serdes.String(), inventoryReservationFailedEventSerde));



        KStream<String, InventoryStatusEvent> inventoryStatusStream = inventoryCompletedStream
                .mapValues(inv -> new InventoryStatusEvent(inv.orderId(), true))
                .merge(inventoryFailedStream.mapValues(inv -> new InventoryStatusEvent(inv.orderId(), false)));


        KStream<String, PaymentStatusEvent> paymentStatusStream = paymentCompletedStream
                .mapValues(pay -> new PaymentStatusEvent(pay.orderId(), true))
                .merge(paymentFailedStream.mapValues(pay -> new PaymentStatusEvent(pay.orderId(), false)));


        KStream<String, OrderStatus> orderStatusStream = inventoryStatusStream
                .leftJoin(
                        paymentStatusStream,
                        (inventory, payment) -> {
                            boolean reserved = inventory.reserved();
                            boolean paid = payment != null && payment.paid();

                            String paymentStatus = (reserved && paid) ? "COMPLETED" : "FAILED";
                            String inventoryStatus = reserved ? "RESERVED" : "FAILED";
                            String finalStatus = !reserved ? "INVENTORY_FAILED" : (paid ? "SUCCESS" : "PAYMENT_FAILED");

                            return new OrderStatus(
                                    inventory.orderId().toString(),
                                    paymentStatus,
                                    inventoryStatus,
                                    finalStatus
                            );
                        },
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)),
                        StreamJoined.with(
                                Serdes.String(),
                                new JsonSerde<>(InventoryStatusEvent.class),
                                new JsonSerde<>(PaymentStatusEvent.class)
                        )
                );

        orderStatusStream.to(
                kafkaTopicProperties.orderStatusUpdates(),
                Produced.with(Serdes.String(), orderStatusSerde)
        );

        return orderStatusStream;
    }
}