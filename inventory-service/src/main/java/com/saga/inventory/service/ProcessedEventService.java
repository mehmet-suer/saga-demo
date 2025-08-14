package com.saga.inventory.service;

import com.saga.inventory.client.kafka.KafkaClient;
import com.saga.inventory.model.entity.ProcessedEvent;
import com.saga.inventory.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import java.util.List;

@org.springframework.stereotype.Service
public class ProcessedEventService {
    private static final Logger log = LoggerFactory.getLogger(ProcessedEventService.class);

    private final ProcessedEventRepository repository;
    private final KafkaClient kafkaClient;
    private final ProcessedEventTxService processedEventTxService;

    public ProcessedEventService(ProcessedEventRepository repository, KafkaClient kafkaClient, ProcessedEventTxService processedEventTxService) {
        this.repository = repository;
        this.kafkaClient = kafkaClient;
        this.processedEventTxService = processedEventTxService;
    }

    public void save(ProcessedEvent processedEvent) {
        repository.save(processedEvent);
    }


    public void process(ProcessedEvent event) {
        try {
            kafkaClient.send(event.getTopic(), event.getKey().toString(), event.getPayload(), event.getHeadersPayload());
            processedEventTxService.markSentWithVersion(event);
        } catch (Exception ex) {
            processedEventTxService.markRetry(event.getId());
            log.warn("Kafka send failed for eventId={}", event.getEventId(), ex);
        }
    }

    public List<ProcessedEvent> findUnsentEventsByType(Pageable pageable) {
        return repository.findUnsentEvents(pageable);
    }
}