package com.saga.inventory.schduler;

import com.saga.inventory.model.entity.ProcessedEvent;
import com.saga.inventory.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class OutboxEventScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventScheduler.class);
    private final ProcessedEventService processedEventService;
    private final ExecutorService outboxExecutor;

    public OutboxEventScheduler(ProcessedEventService processedEventService, ExecutorService outboxExecutor) {
        this.processedEventService = processedEventService;
        this.outboxExecutor = outboxExecutor;
    }

    @Scheduled(fixedDelay = 5_000)
    public void send() {
        List<ProcessedEvent> unsentEvents = processedEventService.findUnsentEventsByType(PageRequest.of(0, 100));
        for (ProcessedEvent event : unsentEvents) {
            outboxExecutor.submit(() -> processedEventService.process(event));
        }
    }
}