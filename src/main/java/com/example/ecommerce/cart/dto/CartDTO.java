package com.example.ecommerce.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDTO {
    private Integer id;
    private Integer userId;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
    private BigDecimal discount;
    private BigDecimal finalPrice;
}