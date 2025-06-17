package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final CartService cartService;

    public CartRestController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Integer userId,
                                            @RequestParam Integer productId,
                                            @RequestParam(defaultValue = "1") int quantity) {
        try {
            cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.ok("Thêm vào giỏ thành công");
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateQuantity(@RequestParam Integer itemId,
                                                 @RequestParam String action) {
        try {
            int delta = "increase".equals(action) ? 1 : -1;
            cartService.updateQuantity(itemId, delta); // Gọi phương thức hiện có
            // Kiểm tra số lượng sau khi cập nhật
            CartItem item = cartService.getCartItemRepository().findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item không tồn tại"));
            int newQuantity = item.getQuantity();
            if (newQuantity <= 0) {
                cartService.removeItem(itemId);
                return ResponseEntity.ok("Đã xóa sản phẩm do số lượng <= 0");
            }
            return ResponseEntity.ok("Cập nhật số lượng thành công: " + newQuantity);
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeItem(@RequestParam Integer itemId) {
        try {
            cartService.removeItem(itemId);
            return ResponseEntity.ok("Đã xóa sản phẩm");
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}