package com.example.ecommerce.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Integer id;
    private LocalDateTime orderTime;
    private BigDecimal totalPrice;
    private String status;
    private String note;
    private String shippingAddress;
    private List<OrderItemDTO> items;
    private String userFullName;
}

