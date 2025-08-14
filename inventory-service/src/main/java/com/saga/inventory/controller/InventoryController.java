package com.saga.inventory.controller;

import com.saga.inventory.model.entity.Inventory;
import com.saga.inventory.service.InventoryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping("/init")
    public ResponseEntity<Void> init(@RequestParam @NotBlank String productId, @RequestParam @Min(0) int qty) {
        service.initProduct(productId, qty);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/init")
    public ResponseEntity<List<Inventory>> get() {
        return ResponseEntity.ok(service.findAll());
    }
}
