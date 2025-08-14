package com.saga.order.controller;

import com.saga.order.model.entity.Order;
import com.saga.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<UUID> createOrder(@RequestParam @NotBlank String userId, @RequestParam @NotBlank String productId) {
        UUID orderId = orderService.createOrder(userId, productId);
        return ResponseEntity.ok(orderId);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getALL() {
        List<Order> orders = orderService.getAll();
        return ResponseEntity.ok(orders);
    }
}