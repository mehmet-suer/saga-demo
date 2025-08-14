package com.saga.order.schduler;

import com.saga.order.model.OrderStatus;
import com.saga.order.model.entity.Order;
import com.saga.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@Component
public class PendingOrderTimeoutScheduler {
    private static final Logger log = LoggerFactory.getLogger(PendingOrderTimeoutScheduler.class);
    private static final List<OrderStatus> PENDING_STATES = List.of(OrderStatus.CREATED, OrderStatus.PAYMENT_COMPLETED);
    private final long timeoutMinutes;
    private final OrderService orderService;
    private final ExecutorService outboxExecutor;

    public PendingOrderTimeoutScheduler(
            @Value("${order.timeout.minutes:10}") long timeoutMinutes,
            OrderService orderService,
            @Qualifier("timedOutOrdersExecutor") ExecutorService outboxExecutor) {
        this.timeoutMinutes = timeoutMinutes;
        this.orderService = orderService;
        this.outboxExecutor = outboxExecutor;
    }

    @Scheduled(fixedDelayString = "${order.timeout.scheduler.delay-ms:10000}")
    @Transactional
    public void cancelStaleOrders() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(timeoutMinutes));
        List<Order> ordersToCancel;
        try {
            ordersToCancel = orderService.findPendingOrdersBeforeCutoff(PENDING_STATES, cutoff, PageRequest.of(0, 100));
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
                                PENDING_STATES,
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