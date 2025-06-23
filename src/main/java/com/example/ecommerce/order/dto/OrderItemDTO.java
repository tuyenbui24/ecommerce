package com.example.ecommerce.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private String productName;
    private BigDecimal price;
    private int quantity;
    private String image;
    private Integer productId;
}

