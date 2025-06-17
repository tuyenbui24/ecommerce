package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.CartDTO;
import com.example.ecommerce.cart.dto.CartItemDTO;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.mapper.CartMapper;
import com.example.ecommerce.cart.repo.CartItemRepository;
import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public CartService(CartRepository cartRepo, CartItemRepository itemRepo,
                       ProductRepository productRepo, UserRepository userRepo) {
        this.cartRepo = cartRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    public CartItemRepository getCartItemRepository() {
        return itemRepo;
    }

    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepo.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .user(user)
                    .build();
            return cartRepo.save(newCart);
        });

        CartDTO cartDTO = CartMapper.toDTO(cart);

        // Tính toán lại giá
        BigDecimal totalPrice = calculateTotalPrice(cartDTO.getItems());
        BigDecimal discount = calculateDiscount(cartDTO.getItems());
        BigDecimal finalPrice = totalPrice.subtract(discount);

        cartDTO.setTotalPrice(totalPrice);
        cartDTO.setDiscount(discount);
        cartDTO.setFinalPrice(finalPrice);

        return cartDTO;
    }

    @Transactional
    public void addToCart(Integer userId, Integer productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepo.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .user(user)
                    .build();
            return cartRepo.save(newCart);
        });

        CartItem existingItem = itemRepo.findByCartAndProduct(cart, product)
                .orElse(null);

        if (existingItem != null) {
            // Cập nhật số lượng nếu item đã tồn tại
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            itemRepo.save(existingItem);
        } else {
            // Tạo item mới
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            itemRepo.save(newItem);

            // Thêm vào collection để đồng bộ
            cart.addItem(newItem);
        }
    }

    @Transactional
    public void removeItem(Integer itemId) {
        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        itemRepo.delete(item);

        // Đảm bảo cascade xóa đúng cách
        cartRepo.save(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Xóa tất cả items
        itemRepo.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepo.save(cart);
    }

    @Transactional
    public void updateQuantity(Integer itemId, int delta) {
        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        int newQuantity = item.getQuantity() + delta;

        if (newQuantity <= 0) {
            removeItem(itemId);
        } else {
            item.setQuantity(newQuantity);
            itemRepo.save(item);
        }
    }

    @Transactional
    public void setQuantity(Integer itemId, int quantity) {
        if (quantity <= 0) {
            removeItem(itemId);
            return;
        }

        CartItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        item.setQuantity(quantity);
        itemRepo.save(item);
    }

    private BigDecimal calculateTotalPrice(List<CartItemDTO> items) {
        return items.stream()
                .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(List<CartItemDTO> items) {
        // Logic giảm giá có thể thêm ở đây
        BigDecimal total = calculateTotalPrice(items);
        if (total.compareTo(new BigDecimal("1000000")) > 0) {
            return total.multiply(new BigDecimal("0.1")); // 10% discount
        }
        return BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public int getCartItemCount(Integer userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return 0;

        Cart cart = cartRepo.findByUser(user).orElse(null);
        if (cart == null) return 0;

        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}