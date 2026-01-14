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

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("select p from Product p where p.name = :name")
    Product getProductByName(@Param("name") String name);

    @Modifying
    @Transactional
    @Query("update Product p set p.enabled = ?2 where p.id = ?1")
    void updateEnabled(Integer id, boolean enabled);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("select p from Product p where concat(p.id, ' ', p.name, ' ', p.price, ' ', p.quantity) like %?1%")
    Page<Product> searchP(String keyword, Pageable pageable);

    @Query("select p from Product p where p.category.id = :catId")
    Page<Product> findPageByCategoryId(@Param("catId") Integer categoryId, Pageable pageable);

    @Query("""
           select p from Product p
           where (:kw is null or :kw = '' or
                  concat(p.id, ' ', p.name, ' ', p.price, ' ', p.quantity) like %:kw%)
             and (:catId is null or p.category.id = :catId)
           """)
    Page<Product> searchByKeywordAndCategory(@Param("kw") String keyword,
                                             @Param("catId") Integer categoryId,
                                             Pageable pageable);

    Page<Product> findByCategory_Id(Integer categoryId, Pageable pageable);
    Page<Product> findByCategory_Name(String name, Pageable pageable);
    long countByCategory_Id(Integer categoryId);

    @Query("""
    SELECT p FROM Product p
    LEFT JOIN ProductSize ps ON ps.product.id = p.id
    WHERE (:kw IS NULL OR :kw = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')))
      AND (:catId IS NULL OR p.category.id = :catId)
      AND (:size IS NULL OR ps.size = :size)
    GROUP BY p.id
    HAVING (COALESCE(SUM(ps.quantity), 0) >= COALESCE(:minStock, 0))
       AND (:maxStock IS NULL OR COALESCE(SUM(ps.quantity), 0) <= :maxStock)
    """)
    Page<Product> filterByStockRange(
            @Param("kw") String keyword,
            @Param("catId") Integer categoryId,
            @Param("size") String size,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            Pageable pageable
    );
    boolean existsByNameIgnoreCase(String name);
}
