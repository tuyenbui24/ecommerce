package com.example.ecommerce.product.mapper;

import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.product.entity.Product;

public class ProductMapper {

    // Chuyển từ Entity sang DTO
    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setImage(product.getImage());
        dto.setEnabled(product.isEnabled());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }

    // Chuyển từ CreateRequest (DTO) sang Entity
    public static Product toEntity(ProductCreateRequest request, Category category) {
        if (request == null || category == null) return null;

        return Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .image(request.getImage() == null || request.getImage().isEmpty()
                        ? Product.DEFAULT_IMAGE
                        : request.getImage())
                .enabled(true)
                .category(category)
                .build();
    }
}
