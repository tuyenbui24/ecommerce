package com.example.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String description;
    private String image;
    private Integer categoryId;
}
