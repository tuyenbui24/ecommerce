package com.example.ecommerce.product.repository;

import com.example.ecommerce.product.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {
    List<ProductSize> findByProduct_Id(Integer productId);
}
