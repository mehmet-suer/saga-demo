package com.saga.inventory.schduler;

import com.saga.common.kafkaoutbox.OutboxProperties;
import com.saga.inventory.model.entity.ProcessedEvent;
import com.saga.inventory.service.ProcessedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

@Component
public class OutboxEventScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventScheduler.class);
    private final ProcessedEventService processedEventService;
    private final ExecutorService outboxExecutor;
    private final int batchSize;
    private final Duration leaseDuration;
    private final Semaphore inflight;

    public OutboxEventScheduler(ProcessedEventService processedEventService,
                                @Qualifier("outboxExecutor") ExecutorService outboxExecutor,
                                OutboxProperties outboxProperties) {
        this.processedEventService = processedEventService;
        this.outboxExecutor = outboxExecutor;
        this.batchSize = outboxProperties.batch().size();
        this.leaseDuration = Duration.ofSeconds(Math.max(1, outboxProperties.lease().seconds()));
        this.inflight = new Semaphore(outboxProperties.inflight().limit());
    }

    @Scheduled(fixedDelay = 5_000)
    public void send() {
        int reservedPermits = reservePermitsForClaim();
        if (reservedPermits == 0) {
            return;
        }

        List<ProcessedEvent> claimedEvents;
        try {
            claimedEvents = processedEventService.claimBatch(reservedPermits, leaseDuration);
        } catch (Exception e) {
            inflight.release(reservedPermits);
            log.error("Failed to claim outbox events", e);
            return;
        }

        releaseUnusedPermits(reservedPermits, claimedEvents.size());
        for (ProcessedEvent event : claimedEvents) {
            try {
                outboxExecutor.submit(() -> processedEventService.process(event)
                        .whenComplete((ack, ex) -> {
                            try {
                                logIfExceptionExists(event, ex);
                            } finally {
                                inflight.release();
                            }
                        }));
            } catch (RejectedExecutionException rex) {
                handleRejectedSubmit(event, rex);
            } catch (Exception e) {
                handleUnexpectedSubmitFailure(event, e);
            }
        }
    }

    private void logIfExceptionExists(ProcessedEvent event, Throwable ex) {
        if (ex != null) {
            Throwable cause = unwrapCompletionException(ex);
            log.error("Outbox processing failed for eventId={}", event.getEventId(), cause);
        }
    }

    private void releaseUnusedPermits(int reservedPermits, int claimedEventsSize) {
        int unusedPermits = reservedPermits - claimedEventsSize;
        if (unusedPermits > 0) {
            inflight.release(unusedPermits);
        }
    }

    private void handleRejectedSubmit(ProcessedEvent event, RejectedExecutionException error) {
        inflight.release();
        markDispatchFailedSafely(event, error);
        log.warn("Outbox executor rejected task; will retry in next tick, event={}", event.getId(), error);
    }

    private void handleUnexpectedSubmitFailure(ProcessedEvent event, Exception error) {
        inflight.release();
        markDispatchFailedSafely(event, error);
        log.error("Submitting outbox task failed for event={}", event.getId(), error);
    }

    private void markDispatchFailedSafely(ProcessedEvent event, Throwable failure) {
        try {
            processedEventService.markDispatchFailed(event.getId(), failure);
        } catch (Exception markEx) {
            log.error("Failed to mark dispatch failed for event={}", event.getId(), markEx);
        }
    }

    private int reservePermitsForClaim() {
        int target = Math.min(batchSize, inflight.availablePermits());
        if (target <= 0) {
            log.info("Outbox inflight limit reached; will retry in next tick");
            return 0;
        }

        int reserved = 0;
        while (reserved < target && inflight.tryAcquire()) {
            reserved++;
        }

        if (reserved == 0) {
            log.info("Failed to reserve inflight permits; will retry in next tick");
        }
        return reserved;
    }

    private Throwable unwrapCompletionException(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }
}
