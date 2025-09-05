package com.example.ecommerce.product.dto;

import lombok.Data;

@Data
public class ProductSizeCreateRequest {
    private String size;
    private Integer quantity;
    private Integer productId;
}
