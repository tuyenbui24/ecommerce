package com.example.ecommerce;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public MainController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        Model model) {

        if (keyword != null && !keyword.isBlank()) {
            Page<ProductDTO> searchPage = productService.listByPage(page, keyword);

            model.addAttribute("searchMode", true);
            model.addAttribute("products", searchPage.getContent());
            model.addAttribute("keyword", keyword);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", searchPage.getTotalPages());
        } else {
            model.addAttribute("searchMode", false);
            model.addAttribute("products", productService.findAllProduct());
            model.addAttribute("categoryProducts", productService.getProductsByCategory(9));
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", 1);
            model.addAttribute("categorySlug", ""); // tránh lỗi Thymeleaf khi phân trang
        }
        return "index";
    }

    @GetMapping("/category/{slug}/page/{pageNum}")
    public String viewProductsByCategory(@PathVariable String slug,
                                         @PathVariable int pageNum,
                                         Model model) {

        Category category = categoryService.getBySlug(slug);
        Page<ProductDTO> page = productService.listByCategory(category.getName(), pageNum);

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("products", page.getContent());
        model.addAttribute("categoryName", category.getName());
        model.addAttribute("categorySlug", slug);
        model.addAttribute("searchMode", false);

        return "category_products";
    }
}
