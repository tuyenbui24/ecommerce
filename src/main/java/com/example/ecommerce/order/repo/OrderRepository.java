package com.example.ecommerce.order.repo;

import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(Integer userId);
}
