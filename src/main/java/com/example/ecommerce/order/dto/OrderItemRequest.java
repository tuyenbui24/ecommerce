package com.example.ecommerce.order.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Integer productId;
    private int quantity;
}
