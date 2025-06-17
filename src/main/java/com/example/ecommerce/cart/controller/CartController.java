package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.user.UserDts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String showCart(Model model) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            model.addAttribute("cart", cartService.getCartByUserId(userId));
            return "cart";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải giỏ hàng: " + e.getMessage());
            return "cart";
        }
    }

    @GetMapping("/{userId}")
    public String showCartByUserId(@PathVariable Integer userId, Model model) {
        try {
            model.addAttribute("cart", cartService.getCartByUserId(userId));
            return "cart";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải giỏ hàng: " + e.getMessage());
            return "cart";
        }
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cartService.addToCart(userId, productId, quantity);
            redirectAttributes.addFlashAttribute("message", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Integer itemId,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(itemId);
            redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/remove/{itemId}/{userId}")
    public String removeItemLegacy(@PathVariable Integer itemId,
                                   @PathVariable Integer userId,
                                   RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(itemId);
            redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/cart/" + userId;
    }

    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("message", "Đã xóa tất cả sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    @GetMapping("/clear/{userId}")
    public String clearCartLegacy(@PathVariable Integer userId,
                                  RedirectAttributes redirectAttributes) {
        try {
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("message", "Đã xóa tất cả sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/cart/" + userId;
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam Integer itemId,
                             @RequestParam String action,
                             RedirectAttributes redirectAttributes) {
        try {
            int delta = action.equals("increase") ? 1 : -1;
            cartService.updateQuantity(itemId, delta);
            redirectAttributes.addFlashAttribute("message", "Đã cập nhật số lượng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update-quantity")
    public String updateQuantity(@RequestParam Integer itemId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.setQuantity(itemId, quantity);
            redirectAttributes.addFlashAttribute("message", "Đã cập nhật số lượng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserDts) {
            UserDts userDts = (UserDts) authentication.getPrincipal();
            return userDts.getId();
        }
        return null;
    }

    @ModelAttribute("cartItemCount")
    public int getCartItemCount() {
        Integer userId = getCurrentUserId();
        if (userId == null) return 0;
        try {
            return cartService.getCartItemCount(userId);
        } catch (Exception e) {
            return 0;
        }
    }
}