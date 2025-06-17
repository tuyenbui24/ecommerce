package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.CartDTO;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.mapper.CartMapper;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repo.UserRepository;
import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.cart.repo.CartItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public CartService(CartRepository cartRepo, CartItemRepository cartItemRepo,
                       ProductRepository productRepo, UserRepository userRepo) {
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    public CartDTO getCartByUserId(Integer userId) {
        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId).orElseThrow();
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepo.save(newCart);
        });

        CartDTO dto = CartMapper.toDTO(cart);
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalPrice(total);
        dto.setDiscount(BigDecimal.ZERO); // tuỳ chính sách
        dto.setFinalPrice(total.subtract(dto.getDiscount()));
        return dto;
    }

    public void addToCart(Integer userId, Integer productId, int quantity) {
        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId).orElseThrow();
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepo.save(newCart);
        });

        Product product = productRepo.findById(productId).orElseThrow();
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepo.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartItemRepo.save(item);
        }
    }

    public void removeItem(Integer cartItemId) {
        cartItemRepo.deleteById(cartItemId);
    }

    public void updateQuantity(Integer itemId, int quantity) {
        CartItem item = cartItemRepo.findById(itemId).orElseThrow();
        item.setQuantity(quantity);
        cartItemRepo.save(item);
    }
}
