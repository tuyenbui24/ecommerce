// src/main/java/com/example/ecommerce/cart/controller/CartMeController.java
package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.dto.CartDTO;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart/me")
public class CartMeResController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    public CartMeResController(CartService cartService, SecurityUtils securityUtils) {
        this.cartService = cartService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<CartDTO> getMyCart() {
        Integer userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Void> addItemToCart(@RequestParam Integer productId,
                                              @RequestParam String size,
                                              @RequestParam(defaultValue = "1") Integer quantity) {
        Integer userId = securityUtils.getCurrentUserId();
        cartService.addToCart(userId, productId, size, quantity);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable Integer itemId,
                                                   @RequestParam Integer quantity) {
        Integer userId = securityUtils.getCurrentUserId();
        cartService.updateQuantity(userId, itemId, quantity);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{itemId}/size")
    public ResponseEntity<Void> updateItemSize(@PathVariable Integer itemId,
                                               @RequestParam String size) {
        Integer userId = securityUtils.getCurrentUserId();
        cartService.updateItemSize(userId, itemId, size);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Integer itemId) {
        Integer userId = securityUtils.getCurrentUserId();
        cartService.removeItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearMyCart() {
        Integer userId = securityUtils.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
