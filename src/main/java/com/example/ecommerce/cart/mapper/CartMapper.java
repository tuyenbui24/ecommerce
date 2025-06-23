package com.example.ecommerce.cart.mapper;

import com.example.ecommerce.cart.dto.CartDTO;
import com.example.ecommerce.cart.dto.CartItemDTO;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.user.entity.User;

import java.util.stream.Collectors;

public class CartMapper {

    // ------------------ Entity → DTO ------------------

    public static CartDTO toDTO(Cart cart) {
        if (cart == null) return null;
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setItems(cart.getItems()
                .stream()
                .map(CartMapper::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public static CartItemDTO toDTO(CartItem item) {
        if (item == null) return null;

        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductPrice(item.getProduct().getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setImage(item.getProduct().getImage());
        return dto;
    }

    // ------------------ DTO → Entity ------------------

    public static Cart toEntity(CartDTO dto, User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cart;
    }

    public static CartItem toEntity(CartItemDTO dto, Cart cart, Product product) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(dto.getQuantity());
        return item;
    }
}

