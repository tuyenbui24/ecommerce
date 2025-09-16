package com.example.ecommerce.product.controller;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.config.FileUpload;
import com.example.ecommerce.config.exception.ProductNotFoundExp;
import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    public ProductRestController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllProduct());
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId) {
        return ResponseEntity.ok(productService.listByPage(page, keyword, categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) throws ProductNotFoundExp {
        return ResponseEntity.ok(productService.getDtoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id,
                                                    @RequestBody ProductCreateRequest req) {
        req.setId(id);
        ProductDTO dto = productService.save(req);
        return ResponseEntity.ok(dto);
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

    @GetMapping("/category")
    public ResponseEntity<Page<ProductDTO>> listByCategory(
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(productService.listByCategory(categoryName, page));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Map<String, List<ProductDTO>>> getProductsByCategory(
            @RequestParam(defaultValue = "4") int num) {
        return ResponseEntity.ok(productService.getProductsByCategory(num));
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<Void> uploadImage(@PathVariable Integer id,
                                            @RequestParam("file") MultipartFile file) throws IOException {
        Product product = productService.getEntityById(id);


        String filename = file.getOriginalFilename();
        String uploadDir = "product-image/" + id;


        FileUpload.cleanDir(uploadDir);
        FileUpload.saveFile(uploadDir, filename, file);


        product.setImage(filename);
        productRepository.save(product);


        return ResponseEntity.ok().build();
    }
}
