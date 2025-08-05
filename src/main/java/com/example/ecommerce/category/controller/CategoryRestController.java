package com.example.ecommerce.category.controller;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.category.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ✅ Lấy tất cả danh mục (không phân trang)
    @GetMapping("/all")
    public ResponseEntity<List<Category>> listAll() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    // ✅ Phân trang danh mục + tìm kiếm
    @GetMapping
    public ResponseEntity<Page<Category>> listByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(categoryService.listByPage(page, keyword));
    }

    // ✅ Lấy danh mục theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    // ✅ Lấy danh mục theo slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    // ✅ Tạo mới hoặc cập nhật danh mục
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody Category category) {
        categoryService.save(category);
        return ResponseEntity.ok().build();
    }

    // ✅ Xóa danh mục theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Kiểm tra tên danh mục có duy nhất không (có thể dùng trong validate form FE)
    @GetMapping("/check-name-unique")
    public ResponseEntity<Boolean> isNameUnique(
            @RequestParam(required = false) Integer id,
            @RequestParam String name) {
        boolean isUnique = categoryService.isNameUnique(id, name);
        return ResponseEntity.ok(isUnique);
    }
}
