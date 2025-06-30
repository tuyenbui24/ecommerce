package com.example.ecommerce.order.service;

import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.mapper.OrderMapper;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.user.repo.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public OrderService(OrderRepository orderRepo, CartRepository cartRepo,
                        ProductRepository productRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    public static final int ITEM_IN_PAGE = 5;

    @Transactional
    public OrderDTO createOrder(Integer userId, String shippingAddress, String note) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNote(note);
        order.setShippingAddress(shippingAddress);

        List<OrderItem> items = cart.getItems().stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            oi.setOrder(order);
            return oi;
        }).toList();

        order.setItems(items);
        order.setTotalPrice(items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        cart.getItems().clear();
        cartRepo.save(cart);
        orderRepo.save(order);
        return OrderMapper.toDTO(order);
    }


    @Transactional(readOnly = true)
    public List<OrderDTO> getOrderHistory(Integer userId) {
        return orderRepo.findByUserId(userId)
                .stream().map(OrderMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public OrderDTO getById(Integer id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return OrderMapper.toDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepo.findAll()
                .stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @Transactional
    public void updateStatus(Integer orderId, String statusStr) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("không tìm thấy đơn hàng"));
        OrderStatus status = OrderStatus.valueOf(statusStr);
        order.setStatus(status);
        orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> findAllOrdersPaged(int pageNum) {
        Pageable pageable = PageRequest.of(pageNum - 1, ITEM_IN_PAGE, Sort.by("orderTime").descending());
        Page<Order> orderPage = orderRepo.findAll(pageable);

        List<OrderDTO> dtos = orderPage.getContent()
                .stream().map(OrderMapper::toDTO).toList();

        return new PageImpl<>(dtos, pageable, orderPage.getTotalElements());
    }
}

