package com.example.ecommerce.order.controller;


import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getOrdersPaged(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(orderService.findAllOrdersPaged(page));
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}