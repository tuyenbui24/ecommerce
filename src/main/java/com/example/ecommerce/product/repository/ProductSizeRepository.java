package com.example.ecommerce.product.repository;

import com.example.ecommerce.product.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {
    List<ProductSize> findByProduct_Id(Integer productId);

    Optional<ProductSize> findByProduct_IdAndSizeIgnoreCase(Integer productId, String size);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ProductSize ps where ps.id = :id")
    int hardDeleteById(@Param("id") Integer id);

    @Query("select coalesce(sum(ps.quantity), 0) from ProductSize ps where ps.product.id = :pid")
    Integer sumQuantityByProductId(@Param("pid") Integer productId);
}
