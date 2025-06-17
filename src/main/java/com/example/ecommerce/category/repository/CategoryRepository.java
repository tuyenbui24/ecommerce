package com.example.ecommerce.category.repository;

import com.example.ecommerce.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);

    @Query("select c from Category c where c.name like %?1%")
    Page<Category> searchC(String keyword, Pageable pageable);
}
