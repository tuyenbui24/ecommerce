package com.example.ecommerce.product.controller;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.config.FileUpload;
import com.example.ecommerce.config.exception.ProductNotFoundExp;
import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String firstPage(Model model) {
        return page(1, "", model);
    }

    @GetMapping("/page/{pageNum}")
    public String page(@PathVariable int pageNum,
                       @RequestParam(name = "keyword", defaultValue = "") String keyword,
                       Model model) {
        var page = productService.listByPage(pageNum, keyword);

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("listProduct", page.getContent());
        model.addAttribute("keyword", keyword);
        return "products";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        ProductCreateRequest request = new ProductCreateRequest();
        List<Category> categoryList = productService.findAllCategory();

        model.addAttribute("product", request);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("pageTitle", "Create New Product");
        return "product_form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") ProductCreateRequest request,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes ra) throws IOException {

        if (!productService.isProductNameUnique(request.getId(), request.getName())) {
            ra.addFlashAttribute("errorMessage", "Tên sản phẩm đã tồn tại.");
            if (request.getId() != null) {
                return "redirect:/edit/" + request.getId();
            }
            return "redirect:/products/new";
        }

        if (!imageFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
            request.setImage(fileName);
        }

        ProductDTO saved = productService.save(request);

        if (!imageFile.isEmpty()) {
            String uploadDir = "product-image/" + saved.getId();
            FileUpload.cleanDir(uploadDir);
            FileUpload.saveFile(uploadDir, saved.getImage(), imageFile);
        }

        ra.addFlashAttribute("message", "Lưu sản phẩm thành công!");
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable("id") Integer id,
                                  Model model,
                                  RedirectAttributes ra) {
        try {
            ProductDTO product = productService.getDtoById(id);
            List<Category> categoryList = productService.findAllCategory();

            ProductCreateRequest request = new ProductCreateRequest();
            request.setId(product.getId());
            request.setName(product.getName());
            request.setPrice(product.getPrice());
            request.setQuantity(product.getQuantity());
            request.setDescription(product.getDescription());
            request.setImage(product.getImage());
            request.setCategoryId(product.getCategoryId());

            model.addAttribute("product", request);
            model.addAttribute("categoryList", categoryList);
            model.addAttribute("pageTitle", "Edit Product");
            return "product_form";

        } catch (ProductNotFoundExp e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            productService.delete(id);
            ra.addFlashAttribute("message", "Đã xoá sản phẩm có ID " + id);
        } catch (ProductNotFoundExp e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/{id}/enabled/{status}")
    public String updateEnabledStatus(@PathVariable("id") Integer id,
                                      @PathVariable("status") boolean status,
                                      RedirectAttributes ra) {
        productService.updateStatus(id, status);
        String statusStr = status ? "mở" : "tắt";
        ra.addFlashAttribute("message", "Đã " + statusStr + " sản phẩm ID " + id);
        return "redirect:/products";
    }

    @GetMapping("/detail/{id}")
    public String showProductDetail(@PathVariable Integer id, Model model) {
        ProductDTO product = productService.getDtoById(id);

        model.addAttribute("product", product);
        return "product_detail";
    }

}
