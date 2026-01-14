package com.example.ecommerce.product.entity;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Column(length = 5000)
    private String description;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private boolean enabled;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductSize> sizes = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

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
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    @Transient
    public Integer getTotalStock() {
        if (sizes == null || sizes.isEmpty()) {
            return quantity;
        }
        return sizes.stream()
                .map(ps -> ps.getQuantity() == null ? 0 : ps.getQuantity())
                .reduce(0, Integer::sum);
    }

    @Transient
    public String getSizeDetail() {
        if (sizes == null || sizes.isEmpty()) {
            return "";
        }
        return sizes.stream()
                .map(ps -> ps.getSize() + ":" + (ps.getQuantity() == null ? 0 : ps.getQuantity()))
                .collect(Collectors.joining(" | "));
    }

//    @Transient
//    public String getProductImagePath() {
//        if (id == null || image == null) return "/images/default-product.png";
//        return "/product-image/" + this.id + "/" + this.image;
//    }
}

