package com.example.ecommerce.order.controller;


import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.dto.OrderRequest;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/me/orders")
public class OrderMeController {


    private final OrderService orderService;
    private final SecurityUtils securityUtils;


    public OrderMeController(OrderService orderService, SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderRequest req) {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(orderService.createOrder(userId, req.getShippingAddress(), req.getNote()));
    }


    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrderHistory() {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(orderService.getOrderHistory(userId));
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(orderService.getOrderOfUser(userId, id));
    }


    @GetMapping("/paged")
    public ResponseEntity<Page<OrderDTO>> getMyOrdersPaged(@RequestParam(defaultValue = "1") int page) {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(orderService.getUserOrdersPaged(userId, page));
    }
}