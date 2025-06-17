package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.security.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(Model model) {
        Integer userId = SecurityUtils.getCurrentUserId();
        model.addAttribute("cart", cartService.getCartByUserId(userId));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") int quantity) {
        Integer userId = SecurityUtils.getCurrentUserId();
        cartService.addToCart(userId, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Integer itemId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(itemId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Integer itemId) {
        cartService.removeItem(itemId);
        return "redirect:/cart";
    }
}
