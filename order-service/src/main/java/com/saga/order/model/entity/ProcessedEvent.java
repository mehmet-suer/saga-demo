package com.saga.order.model.entity;

import com.saga.order.model.EventType;
import com.saga.order.model.ProcessedEventStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID eventId;

    @Column(name = "event_key", nullable = false)
    private UUID key;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Instant processedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    private String topic;

    @Version
    private int version;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "headers_payload", columnDefinition = "TEXT")
    private String headersPayload;


    private boolean sent;

    private int retryCount = 0;

    private Instant lastTriedAt;

    @Enumerated(EnumType.STRING)
    private ProcessedEventStatus status;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    public ProcessedEvent() {}

    public ProcessedEvent(UUID eventId, EventType eventType, String payload, String headerPayload, UUID key, String topic) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.headersPayload = headerPayload;
        this.key = Objects.requireNonNull(key, "event key must not be null");
        this.topic = topic;
        this.sent = false;
        this.status = ProcessedEventStatus.NEW;
        Instant now = Instant.now();
        this.processedAt = now;
        this.createdAt = now;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getLastTriedAt() {
        return lastTriedAt;
    }

    public ProcessedEventStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessedEventStatus status) {
        this.status = status;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
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
