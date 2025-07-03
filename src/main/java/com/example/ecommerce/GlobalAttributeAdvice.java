package com.example.ecommerce;

import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.service.ProductService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalAttributeAdvice {

    private final ProductService productService;

    public GlobalAttributeAdvice(ProductService productService) {
        this.productService = productService;
    }

    @ModelAttribute("categoryProducts")
    public Map<String, List<ProductDTO>> addCategoryProducts() {
        return productService.getProductsByCategory(9);
    }
}
