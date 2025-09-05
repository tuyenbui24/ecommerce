package com.example.ecommerce.product.mapper;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.dto.ProductSizeDTO;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.entity.ProductSize;

import java.util.List;

public class ProductMapper {

    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setDescription(product.getDescription());
        dto.setImage(product.getImage());
        dto.setEnabled(product.isEnabled());
        dto.setSizes(List.of());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategorySlug(product.getCategory().getSlug());
        }
        return dto;
    }

    public static Product toEntity(ProductCreateRequest request, Category category) {
        if (request == null || category == null) return null;

        return Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .description(request.getDescription())
                .image(request.getImage() == null || request.getImage().isEmpty()
                        ? Product.DEFAULT_IMAGE
                        : request.getImage())
                .enabled(true)
                .category(category)
                .build();
    }

    public static ProductSizeDTO toSizeDTO(ProductSize size) {
        if (size == null) return null;
        ProductSizeDTO dto = new ProductSizeDTO();
        dto.setId(size.getId());
        dto.setSize(size.getSize());
        dto.setQuantity(size.getQuantity());
        return dto;
    }

    public static List<ProductSizeDTO> toSizeDTOList(List<ProductSize> sizes) {
        return sizes == null ? List.of() :
                sizes.stream().map(ProductMapper::toSizeDTO).toList();
    }
}
