package com.example.ecommerce;

import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    private final ProductService productService;

    public MainController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String viewHomePage(@RequestParam(name = "keyword", required = false) String keyword,
                               @RequestParam(name = "page", defaultValue = "1") int pageNum,
                               Model model) {
        Page<ProductDTO> page = productService.listByPage(pageNum, keyword);

        model.addAttribute("products", page.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("keyword", keyword);
        return "index";
    }
}
