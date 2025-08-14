package com.saga.payment.service;

import com.saga.payment.model.entity.ProcessedEvent;
import com.saga.payment.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProcessedEventTxService {

    private static final Logger log = LoggerFactory.getLogger(ProcessedEventTxService.class);

    private final ProcessedEventRepository repository;

    public ProcessedEventTxService(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(ProcessedEvent event) {
        repository.bumpRetry(event.getId(), Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSentWithVersion(ProcessedEvent event) {
        var updated = repository.markSentWithVersion(event.getId(), event.getVersion(), Instant.now());
        if (updated == 0) {
            log.debug("markSent skipped (version mismatch or already sent), id={}, version={}", event.getId(), event.getVersion());
        }
    }

}
