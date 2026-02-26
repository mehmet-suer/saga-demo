package com.saga.order.repository;

import com.saga.order.model.InventoryState;
import com.saga.order.model.OrderStatus;
import com.saga.order.model.PaymentState;
import com.saga.order.model.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
                SELECT o
                  FROM Order o
                 WHERE (
                        (o.paymentState = :pendingPaymentState AND o.inventoryState = :pendingInventoryState)
                        OR
                        (o.paymentState = :completedPaymentState AND o.inventoryState = :pendingInventoryState)
                 )
                   AND o.updatedAt <= :cutoff
                 ORDER BY o.updatedAt ASC
            """)
    List<Order> findTimeoutCandidatesBeforeCutoff(@Param("pendingPaymentState") PaymentState pendingPaymentState,
                                                  @Param("completedPaymentState") PaymentState completedPaymentState,
                                                  @Param("pendingInventoryState") InventoryState pendingInventoryState,
                                                  @Param("cutoff") Instant cutoff,
                                                  Pageable pageable);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
              UPDATE Order o
                 SET o.status = :failStatus,
                     o.failureReason = :reason,
                     o.updatedAt = :now,
                     o.version = o.version + 1
               WHERE o.id = :id
                 AND o.version = :expectedVersion
                 AND o.status NOT IN :terminalStatuses
                 AND (
                        (o.paymentState = :pendingPaymentState AND o.inventoryState = :pendingInventoryState)
                        OR
                        (o.paymentState = :completedPaymentState AND o.inventoryState = :pendingInventoryState)
                 )
                 AND o.updatedAt <= :cutoff
            """)
    int markTimedOut(@Param("id") UUID id,
                     @Param("expectedVersion") int expectedVersion,
                     @Param("failStatus") OrderStatus failStatus,
                     @Param("reason") String reason,
                     @Param("now") Instant now,
                     @Param("terminalStatuses") List<OrderStatus> terminalStatuses,
                     @Param("pendingPaymentState") PaymentState pendingPaymentState,
                     @Param("completedPaymentState") PaymentState completedPaymentState,
                     @Param("pendingInventoryState") InventoryState pendingInventoryState,
                     @Param("cutoff") Instant cutoff);
}
