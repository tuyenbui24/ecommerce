package com.example.ecommerce.category.service;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.category.repository.CategoryRepository;
import com.example.ecommerce.config.SlugUtil;
import com.example.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    public static final int CATEGORY_PER_PAGE = 10;

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepo, ProductRepository productRepository) {
        this.categoryRepo = categoryRepo;
        this.productRepository = productRepository;
    }

    public List<Category> listAll() {
        return categoryRepo.findAll(Sort.by("name").ascending());
    }

    public Page<Category> listByPage(int pageNum, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, CATEGORY_PER_PAGE, Sort.by("name").ascending());

        if (keyword != null && !keyword.isBlank()) {
            return categoryRepo.searchC(keyword, pageable);
        }

        return categoryRepo.findAll(pageable);
    }

    public void save(Category category) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(SlugUtil.toSlug(category.getName()));
        }
        categoryRepo.save(category);
    }

    public Category getById(Integer id) {
        Optional<Category> result = categoryRepo.findById(id);
        return result.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục có ID: " + id));
    }

    public Category getBySlug(String slug) {
        Category category = categoryRepo.findBySlug(slug);
        if (category == null) {
            throw new IllegalArgumentException("Không tìm thấy danh mục với slug: " + slug);
        }
        return category;
    }

    @Transactional
    public void delete(Integer id) {
        if (!categoryRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy danh mục có ID: " + id);
        }
        long count = productRepository.countByCategory_Id(id);
        if (count > 0) {
            throw new IllegalStateException("Không thể xoá: còn " + count + " sản phẩm thuộc danh mục này.");
        }
        categoryRepo.deleteById(id);
    }

    public boolean isNameUnique(Integer id, String name) {
        Category existing = categoryRepo.findByName(name);
        if (existing == null) return true;

        return existing.getId().equals(id);
    }
}
