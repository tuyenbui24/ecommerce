package com.example.ecommerce.product.controller;

import com.example.ecommerce.product.dto.ProductSizeCreateRequest;
import com.example.ecommerce.product.dto.ProductSizeDTO;
import com.example.ecommerce.product.service.ProductSizeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-sizes")
public class ProductSizeRestController {

    private final ProductSizeService sizeService;

    public ProductSizeRestController(ProductSizeService sizeService) {
        this.sizeService = sizeService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<ProductSizeDTO>> getSizes(@PathVariable Integer productId) {
        return ResponseEntity.ok(sizeService.getSizesByProduct(productId));
    }

    // Thêm size mới cho product
    @PostMapping
    public ResponseEntity<ProductSizeDTO> addSize(@RequestBody ProductSizeCreateRequest request) {
        return ResponseEntity.ok(sizeService.addSize(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductSizeDTO> updateSize(
            @PathVariable Integer id,
            @RequestBody ProductSizeCreateRequest request) {
        return ResponseEntity.ok(sizeService.updateSize(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSize(@PathVariable Integer id) {
        sizeService.deleteSize(id);
        return ResponseEntity.noContent().build();
    }
}
