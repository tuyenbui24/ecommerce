package com.example.ecommerce.cart.repo;

import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Integer userId);
}
