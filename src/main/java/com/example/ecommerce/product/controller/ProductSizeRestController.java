package com.example.ecommerce.product.controller;

import com.example.ecommerce.product.dto.ProductSizeCreateRequest;
import com.example.ecommerce.product.dto.ProductSizeDTO;
import com.example.ecommerce.product.service.ProductSizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/product-sizes")
public class ProductSizeRestController {

    private final ProductSizeService sizeService;

    public ProductSizeRestController(ProductSizeService sizeService) {
        this.sizeService = sizeService;
    }

    @GetMapping("/by-product/{productId}")
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
    public ResponseEntity<Map<String, Object>> deleteSize(@PathVariable Integer id) {
        sizeService.deleteSize(id);
        return ResponseEntity.ok(Map.of("deleted", true, "id", id));
    }

}
