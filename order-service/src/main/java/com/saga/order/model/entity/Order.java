package com.saga.order.model.entity;

import com.saga.order.model.OrderStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column()
    private String failureReason;

    @Version
    @Column(nullable = false)
    private int version = 0;

    protected Order() {
    }

    public Order(UUID id, String userId, String productId) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.status = OrderStatus.CREATED;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void markPaymentCompleted() {
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.failureReason = null;
    }

    public void markPaymentFailed(String reason) {
        this.status = OrderStatus.PAYMENT_FAILED;
        this.failureReason = reason;
    }

    public void markInventoryReserved() {
        this.status = OrderStatus.INVENTORY_RESERVED;
        this.failureReason = null;
    }

    public void markInventoryFailed(String reason) {
        this.status = OrderStatus.INVENTORY_FAILED;
        this.failureReason = reason;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}