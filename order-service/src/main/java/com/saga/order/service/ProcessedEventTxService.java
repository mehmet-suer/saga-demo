package com.saga.order.service;

import com.saga.order.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProcessedEventTxService {

    private final ProcessedEventRepository repository;

    public ProcessedEventTxService(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long id) {
        var e = repository.findForUpdate(id).orElseThrow();
        if (e.isSent()) return;
        e.setSent(true);
        e.setLastTriedAt(Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(Long id) {
        repository.bumpRetry(id, Instant.now());
    }

}
