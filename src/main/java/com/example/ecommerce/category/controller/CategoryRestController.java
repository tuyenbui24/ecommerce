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

    @GetMapping("/all")
    public ResponseEntity<List<Category>> listAll() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    @GetMapping
    public ResponseEntity<Page<Category>> listByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(categoryService.listByPage(page, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        categoryService.save(category);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody Category category) {
        categoryService.save(category);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-name-unique")
    public ResponseEntity<Boolean> isNameUnique(
            @RequestParam(required = false) Integer id,
            @RequestParam String name) {
        boolean isUnique = categoryService.isNameUnique(id, name);
        return ResponseEntity.ok(isUnique);
    }
}
