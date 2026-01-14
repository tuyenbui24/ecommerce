package com.example.ecommerce.order.controller;

import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.dto.OrderRequest;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderRequest req) {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                orderService.createOrder(userId, req.getShippingAddress(), req.getNote(),
                        req.getPhoneNumber(), req.getPaymentMethod())
        );
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<OrderDTO>> getMyOrdersPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer size
    ) {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(orderService.getUserOrdersPaged(userId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(orderService.getOrderOfUser(userId, id));
    }
}
