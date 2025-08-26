package com.example.ecommerce.order.controller;

import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;

    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestParam Integer userId,
            @RequestParam String shippingAddress,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(orderService.createOrder(userId, shippingAddress, note));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrderHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(orderService.getOrderHistory(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    //Lấy tất cả đơn hàng (không phân trang)
    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    //Lấy đơn hàng có phân trang (cho admin)
    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getOrdersPaged(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(orderService.findAllOrdersPaged(page));
    }
}
