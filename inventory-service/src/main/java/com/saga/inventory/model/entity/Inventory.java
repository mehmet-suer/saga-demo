package com.saga.inventory.model.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    private String productId;

    public Inventory(String productId, int availableQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    private int availableQuantity;

    public Inventory(){}

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}