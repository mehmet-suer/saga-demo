package com.saga.inventory.model.entity;

import com.saga.inventory.model.EventType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID eventId;

    @Column(name = "event_key")
    private UUID key;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Instant processedAt;

    private String topic;

    @Version
    private int version;

    @Lob
    private String payload;

    @Lob
    @Column(name = "headers_payload")
    private String headersPayload;


    private boolean sent;

    private int retryCount = 0;

    private Instant lastTriedAt;

    public ProcessedEvent() {}

    public ProcessedEvent(UUID eventId, EventType eventType, String payload, String headerPayload, UUID key, String topic) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.headersPayload = headerPayload;
        this.key = key;
        this.topic = topic;
        this.sent = false;
        this.processedAt = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isSent() {
        return sent;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setLastTriedAt(Instant now) {
        this.lastTriedAt = now;
    }

    public void incrementRetryCount() {
        this.retryCount = retryCount + 1;
    }

    public Instant getLastTriedAt() {
        return lastTriedAt;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getHeadersPayload() {
        return headersPayload;
    }

    public void setHeadersPayload(String headersPayload) {
        this.headersPayload = headersPayload;
    }

    public Long getId() {
        return id;
    }
}