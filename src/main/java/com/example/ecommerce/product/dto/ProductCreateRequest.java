package com.example.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng tối thiểu là 0")
    private Integer quantity;

    @Size(max = 10000, message = "Mô tả tối đa 5000 ký tự")
    private String description;

    private String image;

    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;
}
