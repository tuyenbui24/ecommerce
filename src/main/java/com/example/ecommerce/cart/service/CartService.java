package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.CartDTO;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.mapper.CartMapper;
import com.example.ecommerce.cart.repo.CartItemRepository;
import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repo.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final EntityManager entityManager;

    public CartService(CartRepository cartRepo, CartItemRepository cartItemRepo,
                       ProductRepository productRepo, UserRepository userRepo,
                       EntityManager entityManager) {
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.entityManager = entityManager;
    }

    @Transactional
    public CartDTO getCartByUserId(Integer userId) {
        // Lấy cart hoặc tạo mới nếu chưa có
        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId).orElseThrow();
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepo.save(newCart);
        });

        // Sau khi có cart, truy vấn lại từ DB để lấy bản cập nhật mới nhất
//        cart = cartRepo.findById(cart.getId()).orElse(cart);
        entityManager.refresh(cart);


        CartDTO dto = CartMapper.toDTO(cart);
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalPrice(total);
        dto.setDiscount(BigDecimal.ZERO); // Tùy chính sách
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

    @Transactional
    public void removeItem(Integer userId, Integer cartItemId) {
        CartItem item = cartItemRepo.findById(cartItemId).orElseThrow();
        Cart cart = item.getCart();

        cart.getItems().remove(item); // Xoá khỏi Set<CartItem>
        cartRepo.save(cart); // JPA sẽ tự xoá do orphanRemoval = true
    }

    public void updateQuantity(Integer itemId, int quantity) {
        CartItem item = cartItemRepo.findById(itemId).orElseThrow();
        item.setQuantity(quantity);
        cartItemRepo.save(item);
    }
}
