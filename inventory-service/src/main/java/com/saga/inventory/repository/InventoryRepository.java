package com.saga.inventory.repository;

import com.saga.inventory.model.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity - 1 WHERE i.id = :id AND i.availableQuantity > 0")
    int tryReserve(@Param("id") String id);
}