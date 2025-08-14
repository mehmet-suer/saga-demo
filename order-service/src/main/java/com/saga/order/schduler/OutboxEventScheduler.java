package com.saga.order.schduler;

import com.saga.order.model.entity.ProcessedEvent;
import com.saga.order.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@Component
public class OutboxEventScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventScheduler.class);
    private final ProcessedEventService processedEventService;
    private final ExecutorService outboxExecutor;

    public OutboxEventScheduler(ProcessedEventService processedEventService,
                                @Qualifier("outboxExecutor") ExecutorService outboxExecutor) {
        this.processedEventService = processedEventService;
        this.outboxExecutor = outboxExecutor;
    }

    @Scheduled(fixedDelay = 5_000)
    public void send() {
        List<ProcessedEvent> unsentEvents;
        try {
            unsentEvents = processedEventService.findUnsentEvents();
        } catch (Exception e) {
            log.error("Outbox fetch failed", e);
            return;
        }

        for (ProcessedEvent event : unsentEvents) {
            try {
                outboxExecutor.submit(() -> processedEventService.process(event));
                ;
            } catch (RejectedExecutionException rex) {
                log.warn("Outbox executor queue is full; will retry in next tick", rex);
                break;
            } catch (Exception e) {
                log.error("Submitting outbox task failed for event ={}", event.getId(), e);
            }
        }
    }
}