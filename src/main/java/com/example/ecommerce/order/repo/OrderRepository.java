package com.example.ecommerce.order.repo;

import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    boolean existsByUser_Id(Integer userId);

    List<Order> findByStatusAndOrderTimeBefore(OrderStatus status, LocalDateTime time);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    @Query("""
           select o from Order o
             left join o.user u
           where (:kw is null or :kw = '' or
                 lower(concat(
                   coalesce(u.firstName,''), ' ', coalesce(u.lastName,''), ' ',
                   coalesce(o.shippingAddress,''),' ',
                   coalesce(u.email,''),' ',
                   coalesce(o.note,''),' ',
                   coalesce(o.phoneNumber,'')
                 )) like lower(concat('%', :kw, '%')))
           """)
    Page<Order> searchByKeyword(@Param("kw") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findByStatusInAndOrderTimeBetween(
            Collection<OrderStatus> statuses,
            LocalDateTime start,
            LocalDateTime end
    );
}
