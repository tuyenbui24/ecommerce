package com.example.ecommerce.product.controller;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.config.FileUpload;
import com.example.ecommerce.config.exception.ProductNotFoundExp;
import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.service.ProductService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String firstPage(Model model) {
        return page(1, "", model);
    }

    // Phân trang + tìm kiếm
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

    // Hiển thị form tạo mới
    @GetMapping("/new")
    public String newProductForm(Model model) {
        ProductCreateRequest request = new ProductCreateRequest();
        List<Category> categoryList = productService.findAllCategory();

        model.addAttribute("product", request);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("pageTitle", "Create New Product");
        return "product_form";
    }

    // Lưu sản phẩm mới hoặc cập nhật
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") ProductCreateRequest request,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes ra) throws IOException {

        // ✅ Kiểm tra tên sản phẩm trùng (trừ khi đang sửa chính nó)
        if (!productService.isProductNameUnique(request.getId(), request.getName())) {
            ra.addFlashAttribute("errorMessage", "Tên sản phẩm đã tồn tại.");
            if (request.getId() != null) {
                return "redirect:/edit/" + request.getId();
            }
            return "redirect:/products/new";
        }

        // ✅ Nếu có ảnh mới → gán tên ảnh
        if (!imageFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
            request.setImage(fileName);
        }

        // ✅ Lưu sản phẩm
        ProductDTO saved = productService.save(request);

        // ✅ Lưu ảnh vào thư mục (nếu có)
        if (!imageFile.isEmpty()) {
            String uploadDir = "product-image/" + saved.getId();
            FileUpload.cleanDir(uploadDir);
            FileUpload.saveFile(uploadDir, saved.getImage(), imageFile);
        }

        ra.addFlashAttribute("message", "Lưu sản phẩm thành công!");
        return "redirect:/products";
    }

    // Form sửa sản phẩm
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

    // Xoá sản phẩm
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

    // Bật/tắt trạng thái hoạt động
    @GetMapping("/{id}/enabled/{status}")
    public String updateEnabledStatus(@PathVariable("id") Integer id,
                                      @PathVariable("status") boolean status,
                                      RedirectAttributes ra) {
        productService.updateStatus(id, status);
        String statusStr = status ? "mở" : "tắt";
        ra.addFlashAttribute("message", "Đã " + statusStr + " sản phẩm ID " + id);
        return "redirect:/products";
    }

    @GetMapping("/product-image/{productId}/{imageName}")
    public ResponseEntity<Resource> serveImage(@PathVariable Integer productId,
                                               @PathVariable String imageName) throws IOException {
        String uploadDir = "product-image/" + productId;
        Path imagePath = Paths.get(uploadDir).resolve(imageName).normalize();
        System.out.println("Checking image path: " + imagePath); // Debug
        try {
            Resource resource = new UrlResource(imagePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(imagePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                        .body(resource);
            } else {
                System.out.println("Image not found or not readable: " + imagePath);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }
}
