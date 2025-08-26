package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.dto.CartDTO;
import com.example.ecommerce.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final CartService cartService;

    public CartRestController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<Void> addToCart(
            @PathVariable Integer userId,
            @RequestParam Integer productId,
            @RequestParam int quantity) {
        cartService.addToCart(userId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Integer itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Void> updateQuantity(
            @PathVariable Integer itemId,
            @RequestParam int quantity) {
        cartService.updateQuantity(itemId, quantity);
        return ResponseEntity.ok().build();
    }
}
