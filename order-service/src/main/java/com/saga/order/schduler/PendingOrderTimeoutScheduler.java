package com.saga.order.schduler;

import com.saga.order.config.OrderTimeoutProperties;
import com.saga.order.model.InventoryState;
import com.saga.order.model.OrderStatus;
import com.saga.order.model.PaymentState;
import com.saga.order.model.constant.ExecutorConstants;
import com.saga.order.model.entity.Order;
import com.saga.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@Component
public class PendingOrderTimeoutScheduler {
    private static final Logger log = LoggerFactory.getLogger(PendingOrderTimeoutScheduler.class);
    private static final PaymentState PENDING_PAYMENT_STATE = PaymentState.PENDING;
    private static final PaymentState COMPLETED_PAYMENT_STATE = PaymentState.COMPLETED;
    private static final InventoryState PENDING_INVENTORY_STATE = InventoryState.PENDING;
    private static final List<OrderStatus> TERMINAL_STATES = List.of(OrderStatus.CANCELLED, OrderStatus.DONE);
    private final long timeoutMinutes;
    private final OrderService orderService;
    private final ExecutorService outboxExecutor;

    public PendingOrderTimeoutScheduler(
            OrderTimeoutProperties orderTimeoutProperties,
            OrderService orderService,
            @Qualifier(ExecutorConstants.TIMED_OUT_ORDERS_EXECUTOR) ExecutorService outboxExecutor) {
        this.timeoutMinutes = orderTimeoutProperties.minutes();
        this.orderService = orderService;
        this.outboxExecutor = outboxExecutor;
    }

    @Scheduled(fixedDelayString = "${order.timeout.scheduler.delay-ms:10000}")
    public void cancelStaleOrders() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(timeoutMinutes));
        List<Order> ordersToCancel;
        try {
            ordersToCancel = orderService.findTimeoutCandidatesBeforeCutoff(
                    PENDING_PAYMENT_STATE,
                    COMPLETED_PAYMENT_STATE,
                    PENDING_INVENTORY_STATE,
                    cutoff,
                    PageRequest.of(0, 100)
            );
        } catch (Exception e) {
            log.error("Fetch orders to cancel failed", e);
            return;
        }
        for (Order order : ordersToCancel) {
            try {
                outboxExecutor.submit(() ->
                        orderService.markTimedOut(
                                order,
                                OrderStatus.CANCELLED,
                                "Timed out after " + timeoutMinutes + " minutes",
                                Instant.now(),
                                TERMINAL_STATES,
                                PENDING_PAYMENT_STATE,
                                COMPLETED_PAYMENT_STATE,
                                PENDING_INVENTORY_STATE,
                                cutoff
                        ));
            } catch (RejectedExecutionException rex) {
                log.warn("Outbox executor queue is full; will retry in next tick", rex);
                break;
            } catch (Exception e) {
                log.error("Submitting outbox task to cancel order = {}", order.getId(), e);
            }
        }
    }
}
