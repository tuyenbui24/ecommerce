package com.example.ecommerce.product.controller;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.config.exception.ProductNotFoundExp;
import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductService productService;

    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllProduct());
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.listByPage(page, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) throws ProductNotFoundExp {
        return ResponseEntity.ok(productService.getDtoById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> saveProduct(@RequestBody ProductCreateRequest request) {
        ProductDTO saved = productService.save(request);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) throws ProductNotFoundExp {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateProductStatus(@PathVariable Integer id, @RequestParam boolean enabled) {
        productService.updateStatus(id, enabled);
        return ResponseEntity.noContent().build();
    }

    // Kiểm tra tên sản phẩm có duy nhất không
    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkProductNameUnique(
            @RequestParam Integer id,
            @RequestParam String name) {
        return ResponseEntity.ok(productService.isProductNameUnique(id, name));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(productService.findAllCategory());
    }

    // Lấy sản phẩm theo danh mục (có phân trang)
    @GetMapping("/category")
    public ResponseEntity<Page<ProductDTO>> listByCategory(
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(productService.listByCategory(categoryName, page));
    }

    // Lấy sản phẩm theo từng danh mục (giới hạn số lượng mỗi loại)
    @GetMapping("/by-category")
    public ResponseEntity<Map<String, List<ProductDTO>>> getProductsByCategory(
            @RequestParam(defaultValue = "4") int num) {
        return ResponseEntity.ok(productService.getProductsByCategory(num));
    }
}
