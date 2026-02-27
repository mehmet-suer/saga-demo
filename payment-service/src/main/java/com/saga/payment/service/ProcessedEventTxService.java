package com.saga.payment.service;

import com.saga.common.kafkaoutbox.OutboxProperties;
import com.saga.common.kafkaoutbox.ProcessedEventStatus;
import com.saga.payment.model.entity.ProcessedEvent;
import com.saga.payment.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class ProcessedEventTxService {

    private static final Duration MAX_BACKOFF = Duration.ofMinutes(5);

    private final ProcessedEventRepository repository;
    private final int maxRetryAttempts;
    private final long retryIntervalSeconds;

    public ProcessedEventTxService(ProcessedEventRepository repository, OutboxProperties outboxProperties) {
        this.repository = repository;
        this.maxRetryAttempts = Math.max(1, outboxProperties.retry().max().attempts());
        this.retryIntervalSeconds = Math.max(1, outboxProperties.retry().interval().seconds());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long id) {
        ProcessedEvent event = repository.findForUpdate(id).orElseThrow();
        if (event.isSent()) {
            return;
        }
        event.setSent(true);
        event.setStatus(ProcessedEventStatus.SENT);
        event.setLockedUntil(null);
        event.setNextRetryAt(null);
        event.setLastTriedAt(Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublishRetryableFailure(Long id) {
        markRetryableFailure(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublishNonRetryableFailure(Long id) {
        markNonRetryableFailure(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDispatchRetryableFailure(Long id) {
        markRetryableFailure(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDispatchNonRetryableFailure(Long id) {
        markNonRetryableFailure(id);
    }

    private void markRetryableFailure(Long id) {
        ProcessedEvent event = findFailureCandidate(id);
        if (event == null) {
            return;
        }

        Instant now = Instant.now();
        prepareFailure(event, now);
        if (event.getRetryCount() >= maxRetryAttempts) {
            markFailed(event);
            return;
        }

        Duration backoff = calculateBackoff(event.getRetryCount());
        event.setStatus(ProcessedEventStatus.RETRY);
        event.setNextRetryAt(now.plus(backoff));
    }

    private void markNonRetryableFailure(Long id) {
        ProcessedEvent event = findFailureCandidate(id);
        if (event == null) {
            return;
        }

        prepareFailure(event, Instant.now());
        markFailed(event);
    }

    private ProcessedEvent findFailureCandidate(Long id) {
        ProcessedEvent event = repository.findForUpdate(id).orElseThrow();
        if (event.isSent() || event.getStatus() == ProcessedEventStatus.FAILED) {
            return null;
        }
        return event;
    }

    private void prepareFailure(ProcessedEvent event, Instant now) {
        event.setLastTriedAt(now);
        event.setLockedUntil(null);
    }

    private void markFailed(ProcessedEvent event) {
        event.setStatus(ProcessedEventStatus.FAILED);
        event.setNextRetryAt(null);
    }

    private Duration calculateBackoff(int retryCount) {
        long cappedMaxSeconds = MAX_BACKOFF.toSeconds();
        long delaySeconds = Math.min(retryIntervalSeconds, cappedMaxSeconds);

        for (int i = 1; i < retryCount && delaySeconds < cappedMaxSeconds; i++) {
            delaySeconds = Math.min(delaySeconds * 2, cappedMaxSeconds);
        }

        return Duration.ofSeconds(delaySeconds);
    }
}
