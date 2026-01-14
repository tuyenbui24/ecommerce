package com.example.ecommerce.order.controller;

import com.example.ecommerce.export.OrderCsvExporter;
import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getOrdersPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String statusText,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                orderService.listOrdersFiltered(page, size, from, to, statusText, keyword)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ) {
        orderService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public void exportOrders(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String statusText,
            @RequestParam(required = false) String keyword,
            HttpServletResponse response
    ) throws IOException {

        var orders = orderService.exportOrders(from, to, statusText, keyword);
        OrderCsvExporter exporter = new OrderCsvExporter();
        exporter.export(orders, response);
    }
}
