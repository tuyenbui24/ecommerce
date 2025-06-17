package com.example.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String image;
    private boolean enabled;
    private Integer categoryId;
    private String categoryName;
}