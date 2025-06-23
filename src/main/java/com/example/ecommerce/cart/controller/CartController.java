package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.repo.CartItemRepository;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.security.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;
    private final SecurityUtils securityUtils;

    public CartController(CartService cartService, CartItemRepository cartItemRepo,
                          ProductRepository productRepo, SecurityUtils securityUtils) {
        this.cartService = cartService;
        this.cartItemRepo = cartItemRepo;
        this.productRepo = productRepo;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String viewCart(Model model) {
        Integer userId = securityUtils.getCurrentUserId();
        model.addAttribute("cart", cartService.getCartByUserId(userId));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        Integer userId = securityUtils.getCurrentUserId();
        cartService.addToCart(userId, productId, quantity);

        Product product = productRepo.findById(productId).orElse(null);
        if (product != null) {
            redirectAttributes.addFlashAttribute("addedProductName", product.getName());
        }
        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Integer itemId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam Integer itemId) {
        Integer userId = securityUtils.getCurrentUserId();
        cartService.removeItem(userId, itemId);
        return "redirect:/cart";
    }


}
