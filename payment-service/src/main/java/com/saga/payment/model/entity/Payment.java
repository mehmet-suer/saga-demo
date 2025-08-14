package com.saga.payment.model.entity;

import com.saga.payment.model.PaymentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID orderId;

    private String userId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Instant createdAt;

    private String reason;

    @Version
    private int version;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }


    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}