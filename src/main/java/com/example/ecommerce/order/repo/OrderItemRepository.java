package com.example.ecommerce.order.repo;

import com.example.ecommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByProduct_Id(Integer productId);
}
