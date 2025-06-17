package com.example.ecommerce.cart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private BigDecimal productPrice;
    private int quantity;
    private String image;
}
