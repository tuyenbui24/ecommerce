package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.CartDTO;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.mapper.CartMapper;
import com.example.ecommerce.cart.repo.CartItemRepository;
import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.entity.ProductSize;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.product.repository.ProductSizeRepository;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repo.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;
    private final ProductSizeRepository productSizeRepo;
    private final UserRepository userRepo;
    private final EntityManager entityManager;

    public CartService(CartRepository cartRepo,
                       CartItemRepository cartItemRepo,
                       ProductRepository productRepo,
                       ProductSizeRepository productSizeRepo,
                       UserRepository userRepo,
                       EntityManager entityManager) {
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.productRepo = productRepo;
        this.productSizeRepo = productSizeRepo;
        this.userRepo = userRepo;
        this.entityManager = entityManager;
    }

    private String toLetter(String s) {
        if (s == null) return null;
        s = s.trim().toUpperCase();
        return switch (s) {
            case "1" -> "S";
            case "2" -> "M";
            case "3" -> "L";
            case "4" -> "XL";
            case "5" -> "XXL";
            default -> s;
        };
    }

    private String toNumberIfKnown(String s) {
        if (s == null) return null;
        return switch (s.trim().toUpperCase()) {
            case "S" -> "1";
            case "M" -> "2";
            case "L" -> "3";
            case "XL" -> "4";
            case "XXL" -> "5";
            default -> s.trim().toUpperCase();
        };
    }


    @Transactional
    public CartDTO getCartByUserId(Integer userId) {
        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId).orElseThrow();
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepo.save(newCart);
        });

        entityManager.refresh(cart);

        CartDTO dto = CartMapper.toDTO(cart);
        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalPrice(total);
        dto.setDiscount(BigDecimal.ZERO);
        dto.setFinalPrice(total);
        return dto;
    }

    private ProductSize findProductSizeFlexible(Integer productId, String anySize) {
        String letter = toLetter(anySize);
        return productSizeRepo.findByProduct_IdAndSizeIgnoreCase(productId, letter)
                .or(() -> productSizeRepo.findByProduct_IdAndSizeIgnoreCase(productId, toNumberIfKnown(letter)))
                .orElseThrow(() -> new RuntimeException("Size không tồn tại cho sản phẩm"));
    }

    @Transactional
    public void addToCart(Integer userId, Integer productId, String size, int quantity) {
        if (quantity <= 0) throw new RuntimeException("Số lượng phải > 0");
        if (size == null || size.isBlank()) throw new RuntimeException("Vui lòng chọn size");

        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId).orElseThrow();
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepo.save(newCart);
        });

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductSize ps = findProductSizeFlexible(productId, size);
        String canonical = ps.getSize();

        int stock = ps.getQuantity();

        CartItem item = cartItemRepo
                .findByCart_IdAndProduct_IdAndSizeIgnoreCase(cart.getId(), productId, canonical)
                .orElse(null);

        int newQty = (item == null ? 0 : item.getQuantity()) + quantity;
        if (newQty > stock) throw new RuntimeException("Số lượng vượt quá tồn kho size " + toLetter(canonical));

        if (item == null) {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .size(canonical)
                    .quantity(quantity)
                    .build();
        } else {
            item.setQuantity(newQty);
        }
        cartItemRepo.save(item);
    }

    @Transactional
    public void updateItemSize(Integer userId, Integer itemId, String newSize) {
        if (newSize == null || newSize.isBlank()) throw new RuntimeException("Vui lòng chọn size");
        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng"));

        if (!item.getCart().getUser().getId().equals(userId))
            throw new RuntimeException("Không có quyền cập nhật mục này");

        ProductSize ps = findProductSizeFlexible(item.getProduct().getId(), newSize);
        if (ps.getQuantity() < item.getQuantity())
            throw new RuntimeException("Số lượng vượt tồn kho của size " + ps.getSize());

        item.setSize(ps.getSize());
        cartItemRepo.save(item);
    }


    @Transactional
    public void updateQuantity(Integer userId, Integer itemId, int quantity) {
        if (quantity <= 0) throw new RuntimeException("Số lượng phải > 0");
        CartItem item = cartItemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng"));

        if (!item.getCart().getUser().getId().equals(userId))
            throw new RuntimeException("Không có quyền cập nhật mục này");

        ProductSize ps = productSizeRepo.findByProduct_IdAndSizeIgnoreCase(item.getProduct().getId(), item.getSize())
                .orElseThrow(() -> new RuntimeException("Size không tồn tại"));
        if (quantity > ps.getQuantity())
            throw new RuntimeException("Số lượng vượt quá tồn kho size " + item.getSize());

        item.setQuantity(quantity);
        cartItemRepo.save(item);
    }

    @Transactional
    public void removeItem(Integer userId, Integer cartItemId) {
        CartItem item = cartItemRepo.findById(cartItemId).orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng"));
        if (!item.getCart().getUser().getId().equals(userId))
            throw new RuntimeException("Không có quyền xoá mục này");

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartRepo.save(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Chưa có giỏ hàng"));
        cart.getItems().clear();
        cartRepo.save(cart);
    }
}
