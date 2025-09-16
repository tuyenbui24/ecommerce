package com.example.ecommerce.order.service;

import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.mapper.OrderMapper;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.entity.ProductSize;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.product.repository.ProductSizeRepository;
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
    private final ProductSizeRepository productSizeRepository;

    public OrderService(OrderRepository orderRepo, CartRepository cartRepo,
                        ProductRepository productRepo, UserRepository userRepo,
                        ProductSizeRepository productSizeRepository) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.productSizeRepository = productSizeRepository;
    }

    public static final int ITEM_IN_PAGE = 5;

    @Transactional
    public OrderDTO createOrder(Integer userId, String shippingAddress, String note) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNote(note);
        order.setShippingAddress(shippingAddress);

        List<OrderItem> items = cart.getItems().stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            oi.setSize(ci.getSize());
            return oi;
        }).toList();

        order.setItems(items);

        for (OrderItem oi : items) {
            Integer pId = oi.getProduct().getId();

            ProductSize ps = productSizeRepository
                    .findByProduct_IdAndSizeIgnoreCase(pId, oi.getSize())
                    .orElseThrow(() -> new RuntimeException("Size không tồn tại trong kho"));

            if (ps.getQuantity() < oi.getQuantity()) {
                throw new RuntimeException("Không đủ tồn kho size " + oi.getSize());
            }

            ps.setQuantity(ps.getQuantity() - oi.getQuantity());
            productSizeRepository.save(ps);

            Integer sum = productSizeRepository.sumQuantityByProductId(pId);
            Product p = oi.getProduct();
            p.setQuantity(sum);
            productRepo.save(p);
        }

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);

        orderRepo.save(order);

        cart.getItems().clear();
        cartRepo.save(cart);

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

    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrdersPaged(Integer userId, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum - 1, ITEM_IN_PAGE, Sort.by("orderTime").descending());
        Page<Order> orderPage = orderRepo.findByUserId(userId, pageable);

        List<OrderDTO> dtos = orderPage.getContent()
                .stream().map(OrderMapper::toDTO).toList();

        return new PageImpl<>(dtos, pageable, orderPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderOfUser(Integer userId, Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xem đơn hàng này");
        }

        return OrderMapper.toDTO(order);
    }

}

