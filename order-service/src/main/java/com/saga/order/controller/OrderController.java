package com.saga.order.controller;

import com.saga.order.model.dto.request.CreateOrderRequest;
import com.saga.order.model.entity.Order;
import com.saga.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody @Valid CreateOrderRequest req) {
        orderService.createOrder(req);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Order>> getALL() {
        List<Order> orders = orderService.getAll();
        return ResponseEntity.ok(orders);
    }
}