package com.saga.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.saga.order.client.kafka.KafkaClient;
import com.saga.order.exception.EventPublishException;
import com.saga.order.model.entity.ProcessedEvent;
import com.saga.order.repository.ProcessedEventRepository;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

@Service
public class ProcessedEventService {
    private static final Logger log = LoggerFactory.getLogger(ProcessedEventService.class);

    private final ProcessedEventRepository repository;
    private final KafkaClient kafkaClient;
    private final ProcessedEventTxService processedEventTxService;
    private final ExecutorService outboxExecutor;

    public ProcessedEventService(ProcessedEventRepository repository,
                                 KafkaClient kafkaClient,
                                 ProcessedEventTxService processedEventTxService,
                                 @Qualifier("outboxExecutor") ExecutorService outboxExecutor) {
        this.repository = repository;
        this.kafkaClient = kafkaClient;
        this.processedEventTxService = processedEventTxService;
        this.outboxExecutor = outboxExecutor;
    }

    public void save(ProcessedEvent processedEvent) {
        repository.save(processedEvent);
    }

    public CompletableFuture<Void> process(ProcessedEvent event) {
        try {
            String key = event.getKey().toString();
            return kafkaClient.send(event.getTopic(), key, event.getPayload(), event.getHeadersPayload())
                    .handleAsync((ack, ex) -> {
                        handleProcessedEventState(event, ex);
                        return null;
                    }, outboxExecutor);
        } catch (Exception ex) {
            return handleSendSetupFailure(event, ex);
        }
    }

    private void handleProcessedEventState(ProcessedEvent event, Throwable ex) {
        Throwable cause = unwrapCompletionException(ex);
        try {
            if (cause == null) {
                processedEventTxService.markSent(event.getId());
            } else {
                if (isRetryableFailure(cause)) {
                    processedEventTxService.markPublishRetryableFailure(event.getId());
                } else {
                    processedEventTxService.markPublishNonRetryableFailure(event.getId());
                }
                logPublishFailure(event, cause);
            }
        } catch (Exception stateEx) {
            log.error("Failed to update outbox state for eventId={}", event.getEventId(), stateEx);
            throw new CompletionException(stateEx);
        }
    }

    private CompletableFuture<Void> handleSendSetupFailure(ProcessedEvent event, Exception setupEx) {
        try {
            markDispatchFailed(event.getId(), setupEx);
        } catch (Exception stateEx) {
            stateEx.addSuppressed(setupEx);
            log.error("Kafka send setup and outbox dispatch state update failed for eventId={}", event.getEventId(), stateEx);
            return CompletableFuture.failedFuture(stateEx);
        }
        log.warn("Kafka send setup failed for eventId={}", event.getEventId(), setupEx);
        return CompletableFuture.failedFuture(setupEx);
    }

    @Transactional
    public List<ProcessedEvent> claimBatch(int limit, Duration lease) {
        return repository.claimBatch(limit, lease);
    }

    public void markDispatchFailed(Long id, Throwable failure) {
        Throwable cause = unwrapCompletionException(failure);
        if (isRetryableFailure(cause)) {
            processedEventTxService.markDispatchRetryableFailure(id);
            return;
        }
        processedEventTxService.markDispatchNonRetryableFailure(id);
    }

    private Throwable unwrapCompletionException(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }

    private void logPublishFailure(ProcessedEvent event, Throwable cause) {
        if (cause instanceof EventPublishException epe) {
            log.warn("Kafka send failed for eventId={} topic={} key={}",
                    event.getEventId(), epe.getTopic(), epe.getKey(), cause);
            return;
        }
        log.warn("Kafka send failed for eventId={}", event.getEventId(), cause);
    }

    private boolean isRetryableFailure(Throwable cause) {
        if (cause == null) {
            return true;
        }

        if (cause instanceof EventPublishException epe) {
            if (epe.getCause() == null) {
                return false;
            }
            cause = unwrapCompletionException(epe.getCause());
        }

        return !(cause instanceof JsonProcessingException
                || cause instanceof SerializationException
                || cause instanceof IllegalArgumentException);
    }
}
