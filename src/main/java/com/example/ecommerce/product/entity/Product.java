package com.example.ecommerce.product.entity;

import com.example.ecommerce.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"image", "enabled", "category"})
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "products")
public class Product {
    public static final String DEFAULT_IMAGE = "default_image.jpg";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 150, unique = true, nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Product(String name, BigDecimal price, Integer quantity, String image, Category category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.image = (image == null || image.isEmpty()) ? DEFAULT_IMAGE : image;
        this.category = category;
        this.enabled = true;
    }

    @Transient
    public String getProductImagePath() {
        if (id == null || image == null) return "/images/default-product.png";
        return "/product-image/" + this.id + "/" + this.image;
    }
}

