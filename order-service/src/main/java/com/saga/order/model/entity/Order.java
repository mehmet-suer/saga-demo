package com.saga.order.model.entity;

import com.saga.order.model.OrderStatus;
import com.saga.order.model.InventoryState;
import com.saga.order.model.PaymentState;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentState paymentState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InventoryState inventoryState;

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
        this.paymentState = PaymentState.PENDING;
        this.inventoryState = InventoryState.PENDING;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.paymentState == null) {
            this.paymentState = PaymentState.PENDING;
        }
        if (this.inventoryState == null) {
            this.inventoryState = InventoryState.PENDING;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void markPaymentCompleted() {
        if (isTerminal() || this.paymentState == PaymentState.FAILED || this.paymentState == PaymentState.COMPLETED) {
            return;
        }
        if (this.inventoryState == InventoryState.FAILED) {
            return;
        }
        this.paymentState = PaymentState.COMPLETED;
        this.failureReason = null;
        refreshAggregateStatus();
    }

    public void markPaymentFailed(String reason) {
        if (isTerminal() || this.paymentState == PaymentState.FAILED) {
            return;
        }
        this.paymentState = PaymentState.FAILED;
        this.failureReason = reason;
        refreshAggregateStatus();
    }

    public void markInventoryReserved() {
        if (isTerminal() || this.inventoryState == InventoryState.FAILED || this.inventoryState == InventoryState.RESERVED) {
            return;
        }
        if (this.paymentState == PaymentState.FAILED) {
            return;
        }
        this.inventoryState = InventoryState.RESERVED;
        this.failureReason = null;
        refreshAggregateStatus();
    }

    public void markInventoryFailed(String reason) {
        if (isTerminal() || this.inventoryState == InventoryState.FAILED) {
            return;
        }
        if (this.inventoryState == InventoryState.RESERVED || this.paymentState == PaymentState.FAILED) {
            return;
        }
        this.inventoryState = InventoryState.FAILED;
        this.failureReason = reason;
        refreshAggregateStatus();
    }

    private void refreshAggregateStatus() {
        if (isTerminal()) {
            return;
        }
        if (this.paymentState == PaymentState.FAILED) {
            this.status = OrderStatus.PAYMENT_FAILED;
            return;
        }
        if (this.inventoryState == InventoryState.FAILED) {
            this.status = OrderStatus.INVENTORY_FAILED;
            return;
        }
        if (this.paymentState == PaymentState.COMPLETED && this.inventoryState == InventoryState.RESERVED) {
            this.status = OrderStatus.DONE;
            return;
        }
        if (this.paymentState == PaymentState.COMPLETED) {
            this.status = OrderStatus.PAYMENT_COMPLETED;
            return;
        }
        this.status = OrderStatus.CREATED;
    }

    private boolean isTerminal() {
        return this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DONE;
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

    public PaymentState getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(PaymentState paymentState) {
        this.paymentState = paymentState;
    }

    public InventoryState getInventoryState() {
        return inventoryState;
    }

    public void setInventoryState(InventoryState inventoryState) {
        this.inventoryState = inventoryState;
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
