package com.example.ecommerce.category.controller;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.category.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private static final int CATEGORY_PER_PAGE = 5;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String firstPage(Model model) {
        return listByPage(1, "", model);
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable int pageNum,
                             @RequestParam(name = "keyword", defaultValue = "") String keyword,
                             Model model) {

        Page<Category> page = categoryService.listByPage(pageNum, keyword);
        long start = (long) (pageNum - 1) * CATEGORY_PER_PAGE + 1;
        long end = start + CATEGORY_PER_PAGE - 1;
        if (end > page.getTotalElements()) end = page.getTotalElements();

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("listCategories", page.getContent());
        model.addAttribute("keyword", keyword);

        return "categories";
    }

    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "category_form";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               RedirectAttributes ra) {
        boolean isUnique = categoryService.isNameUnique(category.getId(), category.getName());
        if (!isUnique) {
            ra.addFlashAttribute("errorMessage", "Tên danh mục đã tồn tại!");
            return "redirect:/categories/edit/" + category.getId();
        }

        categoryService.save(category);
        ra.addFlashAttribute("message", "Đã lưu danh mục thành công!");
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        try {
            Category category = categoryService.getById(id);
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", "Chỉnh sửa danh mục");
            return "category_form";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories/edit/";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id,
                                 RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("message", "Đã xoá danh mục có ID " + id);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Xoá không thành công: " + e.getMessage());
        }
        return "redirect:/categories";
    }
}
