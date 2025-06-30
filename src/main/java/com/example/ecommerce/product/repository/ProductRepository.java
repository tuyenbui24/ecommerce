package com.example.ecommerce.product.repository;

import com.example.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("select p from Product p where p.name = :name")
    Product getProductByName(@Param("name") String name);

    @Modifying
    @Transactional
    @Query("update Product p set p.enabled = ?2 where p.id = ?1")
    void updateEnabled(Integer id, boolean enabled);

    @Query("select p from Product p where concat(p.id, ' ', p.name, ' ', p.price, ' ', p.quantity) like %?1%")
    Page<Product> searchP(String keyword, Pageable pageable);

    List<Product> findByCategory_Id(Integer categoryId, Pageable pageable);
    Page<Product> findByCategory_Name(String name, Pageable pageable);
}
