package com.example.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String description;
    private String image;
    private boolean enabled;
    private Integer categoryId;
    private String categoryName;
    private String categorySlug;
    private List<ProductSizeDTO> sizes;
}