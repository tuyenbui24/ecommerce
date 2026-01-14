package com.example.ecommerce.order.service;

import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.entity.PaymentMethod;
import com.example.ecommerce.order.mapper.OrderMapper;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.entity.ProductSize;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.product.repository.ProductSizeRepository;
import com.example.ecommerce.user.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
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

    public static final int DEFAULT_PAGE_SIZE = 8;

    @Transactional
    public OrderDTO createOrder(Integer userId, String shippingAddress, String note,
                                String phoneNumber, PaymentMethod paymentMethod) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));
        if (cart.getItems().isEmpty()) throw new RuntimeException("Giỏ hàng trống");
        if (shippingAddress == null || shippingAddress.isBlank())
            throw new RuntimeException("Địa chỉ giao hàng không được để trống");

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderTime(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.COD);
        order.setStatus(order.getPaymentMethod() == PaymentMethod.COD
                ? OrderStatus.CONFIRMED
                : OrderStatus.PENDING);
        order.setNote(note);
        order.setShippingAddress(shippingAddress);
        order.setPhoneNumber(phoneNumber);

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
    public Page<OrderDTO> findAllOrdersPaged(int pageNum, Integer sizeOpt) {
        int size = (sizeOpt == null || sizeOpt <= 0) ? DEFAULT_PAGE_SIZE : sizeOpt;
        Pageable pageable = PageRequest.of(
                Math.max(pageNum - 1, 0),
                size,
                Sort.by("orderTime").descending().and(Sort.by("id").descending())
        );
        Page<Order> page = orderRepo.findAll(pageable);
        return page.map(OrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrdersPaged(Integer userId, int pageNum, Integer sizeOpt) {
        int size = (sizeOpt == null || sizeOpt <= 0) ? DEFAULT_PAGE_SIZE : sizeOpt;
        Pageable pageable = PageRequest.of(
                Math.max(pageNum - 1, 0),
                size,
                Sort.by("orderTime").descending().and(Sort.by("id").descending())
        );
        Page<Order> page = orderRepo.findByUserId(userId, pageable);
        return page.map(OrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public OrderDTO getById(Integer id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return OrderMapper.toDTO(order);
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

    @Transactional
    public void updateStatus(Integer orderId, String statusStr) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("không tìm thấy đơn hàng"));
        OrderStatus status = OrderStatus.valueOf(statusStr);
        order.setStatus(status);
        orderRepo.save(order);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (o.getStatus() != OrderStatus.CANCELED
                && o.getStatus() != OrderStatus.FAILED
                && o.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể xoá đơn đã huỷ / thất bại / hoàn tất");
        }
        orderRepo.delete(o);
    }

    @Transactional(readOnly = true)
    public void assertOrderReady(Integer orderId, long amountVnd) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (!(o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.FAILED)) {
            throw new IllegalStateException("Chỉ có thể thanh toán lại đơn đang chờ thanh toán hoặc thất bại.");
        }

        if (o.getTotalPrice() == null) {
            throw new RuntimeException("Đơn hàng chưa có tổng tiền");
        }

        long dbAmountVnd = MULTIPLY_1000
                ? o.getTotalPrice().multiply(java.math.BigDecimal.valueOf(1000L)).longValueExact()
                : o.getTotalPrice().longValueExact();

        if (dbAmountVnd != amountVnd) {
            throw new RuntimeException("Số tiền không khớp");
        }
    }

    @Transactional
    public void markPaidByVnpay(Integer orderId, long amountVnd, Map<String,String> p) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.CONFIRMED) return;
        if (o.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Trạng thái đơn không hợp lệ để xác nhận thanh toán");
        }

        long dbAmountVnd = MULTIPLY_1000
                ? o.getTotalPrice().multiply(BigDecimal.valueOf(1000L)).longValueExact()
                : o.getTotalPrice().longValueExact();
        if (dbAmountVnd != amountVnd) {
            throw new RuntimeException("Số tiền IPN không khớp");
        }

        o.setStatus(OrderStatus.PAID);
        o.setPaymentMethod(PaymentMethod.VNPAY);
        o.setGatewayTxnNo(p.get("vnp_TransactionNo"));
        String payDate = p.get("vnp_PayDate");
        if (payDate != null && payDate.matches("\\d{14}")) {
            int yy = Integer.parseInt(payDate.substring(0,4));
            int MM = Integer.parseInt(payDate.substring(4,6));
            int dd = Integer.parseInt(payDate.substring(6,8));
            int hh = Integer.parseInt(payDate.substring(8,10));
            int mm = Integer.parseInt(payDate.substring(10,12));
            int ss = Integer.parseInt(payDate.substring(12,14));
            o.setPaidAt(LocalDateTime.of(yy, MM, dd, hh, mm, ss));
        } else {
            o.setPaidAt(LocalDateTime.now());
        }
        orderRepo.save(o);
    }

    private void restock(Order o) {
        for (OrderItem oi : o.getItems()) {
            Integer pId = oi.getProduct().getId();
            String size = oi.getSize();

            var psOpt = productSizeRepository.findByProduct_IdAndSizeIgnoreCase(pId, size);

            if (psOpt.isPresent()) {
                var ps = psOpt.get();
                ps.setQuantity(ps.getQuantity() + oi.getQuantity());
                productSizeRepository.save(ps);

                Integer sum = productSizeRepository.sumQuantityByProductId(pId);
                Product p = oi.getProduct();
                p.setQuantity(sum != null ? sum : 0);
                productRepo.save(p);
            } else {
                ProductSize psNew = new ProductSize();
                psNew.setProduct(oi.getProduct());
                psNew.setSize(size);
                psNew.setQuantity(oi.getQuantity());
                productSizeRepository.save(psNew);

                Integer sum = productSizeRepository.sumQuantityByProductId(pId);
                Product p = oi.getProduct();
                p.setQuantity(sum != null ? sum : 0);
                productRepo.save(p);
            }
        }
    }

    @Value("${order.pending-ttl-minutes:30}")
    private int pendingTtlMinutes;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cancelExpiredPendingOrders() {
        LocalDateTime limit = LocalDateTime.now().minusMinutes(pendingTtlMinutes);
        List<Order> olds = orderRepo.findByStatusAndOrderTimeBefore(OrderStatus.PENDING, limit);

        for (Order o : olds) {
            try {
                restock(o);
                o.setStatus(OrderStatus.CANCELED);
                String note = (o.getNote() == null ? "" : o.getNote() + " | ") + "Đơn đã bị huỷ";
                o.setNote(note);
                orderRepo.save(o);
            } catch (Exception ex) {
                log.error("Tự động huỷ orderId={}", o.getId(), ex);
            }
        }
    }

    @Transactional
    public void markFailedByVnpay(Integer orderId, long amountVnd, Map<String,String> p) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (o.getStatus() == OrderStatus.PAID) return;

        if (o.getStatus() == OrderStatus.PENDING) {
            restock(o);
        }
        o.setStatus(OrderStatus.FAILED);
        String rsp = p != null ? p.getOrDefault("vnp_ResponseCode", "") : "";
        o.setNote((o.getNote() == null ? "" : o.getNote() + " | ") + "Thanh toán VNPAY thất bại =" + rsp);
        orderRepo.save(o);
    }

    private static final boolean MULTIPLY_1000 = true;

    @Transactional(readOnly = true)
    public long getAmountVnd(Integer orderId) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (o.getTotalPrice() == null) throw new RuntimeException("Đơn hàng chưa có tổng tiền");

        if (MULTIPLY_1000) {
            return o.getTotalPrice().multiply(java.math.BigDecimal.valueOf(1000L)).longValueExact();
        } else {
            return o.getTotalPrice().longValueExact();
        }
    }

    private LocalDate tryParseDateOnly(String s) {
        if (s == null) return null;
        String x = s.trim();
        String[] fmts = {"yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy"};
        for (String f : fmts) {
            try { return LocalDate.parse(x, DateTimeFormatter.ofPattern(f)); }
            catch (Exception ignored) {}
        }
        return null;
    }

    private String viStatusToEn(String vi) {
        if (vi == null || vi.isBlank()) return null;
        String low = vi.trim().toLowerCase(Locale.ROOT);
        switch (low) {
            case "chờ xử lý": return "PENDING";
            case "đã xác nhận": return "CONFIRMED";
            case "đã thanh toán": return "PAID";
            case "đang xử lý": return "PROCESSING";
            case "đang giao": return "SHIPPED";
            case "hoàn tất": return "COMPLETED";
            case "thất bại": return "FAILED";
            case "đã hủy":
            case "đã huỷ": return "CANCELED";
            default: return null;
        }
    }

    private String detectEnStatusFromEitherViOrEn(String s) {
        String en = viStatusToEn(s);
        if (en != null) return en;
        try { return OrderStatus.valueOf(s.trim().toUpperCase(Locale.ROOT)).name(); }
        catch (Exception ignored) { return null; }
    }
    private LocalDateTime parseAnyIsoDateTime(String s, LocalDateTime fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            return OffsetDateTime.parse(s.trim()).toLocalDateTime();
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(s.trim());
            } catch (DateTimeParseException e2) {
                return fallback;
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> listOrdersFiltered(
            int pageNum, Integer sizeOpt,
            String fromIso, String toIso,
            String statusTextVi,
            String keyword
    ) {
        int size = (sizeOpt == null || sizeOpt <= 0) ? DEFAULT_PAGE_SIZE : sizeOpt;
        Pageable pageable = PageRequest.of(
                Math.max(pageNum - 1, 0),
                size,
                Sort.by("orderTime").descending().and(Sort.by("id").descending())
        );

        boolean hasRange = (fromIso != null && !fromIso.isBlank()) || (toIso != null && !toIso.isBlank());
        boolean hasStatus = (statusTextVi != null && !statusTextVi.isBlank());
        boolean hasKw = (keyword != null && !keyword.isBlank());

        if (hasRange) {
            LocalDateTime start = parseAnyIsoDateTime(fromIso, LocalDateTime.MIN);
            LocalDateTime end   = parseAnyIsoDateTime(toIso,   LocalDateTime.MAX);
            Page<Order> page = orderRepo.findByOrderTimeBetween(start, end, pageable);
            List<Order> content = page.getContent();
            if (hasStatus) {
                String stEn = detectEnStatusFromEitherViOrEn(statusTextVi);
                if (stEn != null) {
                    OrderStatus want = OrderStatus.valueOf(stEn);
                    content = content.stream()
                            .filter(o -> o.getStatus() == want)
                            .toList();
                }
            }
            if (hasKw) {
                String like = keyword.trim().toLowerCase();
                final String likeFinal = like; // (để dùng trong lambda)
                content = content.stream().filter(o -> {
                    String full = ((o.getUser() != null ? (o.getUser().getFirstName() + " " + o.getUser().getLastName()) : "") + " " +
                            (o.getUser() != null ? o.getUser().getEmail() : "") + " " +
                            (o.getShippingAddress() != null ? o.getShippingAddress() : "") + " " +
                            (o.getNote() != null ? o.getNote() : "") + " " +
                            (o.getPhoneNumber() != null ? o.getPhoneNumber() : ""))
                            .toLowerCase();

                    if (likeFinal.matches("\\d+")) {
                        return String.valueOf(o.getId()).equals(likeFinal) || full.contains(likeFinal);
                    }
                    return full.contains(likeFinal);
                }).toList();
            }
            return new PageImpl<>(content.stream().map(OrderMapper::toDTO).toList(),
                    pageable,
                    content.size());
        }

        if (hasStatus) {
            String stEn = detectEnStatusFromEitherViOrEn(statusTextVi);
            if (stEn != null) {
                Page<Order> page = orderRepo.findByStatus(OrderStatus.valueOf(stEn), pageable);
                if (hasKw) {
                    String like = keyword.trim().toLowerCase();
                    var filtered = page.stream().filter(o -> {
                        String full = ((o.getUser() != null ? o.getUser().getFullName() : "") + " " +
                                (o.getShippingAddress() != null ? o.getShippingAddress() : "") + " " +
                                (o.getNote() != null ? o.getNote() : "") + " " +
                                (o.getPhoneNumber() != null ? o.getPhoneNumber() : ""))
                                .toLowerCase();
                        if (like.matches("\\d+")) {
                            return String.valueOf(o.getId()).equals(like) || full.contains(like);
                        }
                        return full.contains(like);
                    }).map(OrderMapper::toDTO).toList();
                    return new PageImpl<>(filtered, pageable, filtered.size());
                }
                return page.map(OrderMapper::toDTO);
            }
        }

        if (hasKw && keyword.trim().matches("\\d+")) {
            Integer id = Integer.parseInt(keyword.trim());
            var one = orderRepo.findById(id).map(OrderMapper::toDTO);
            var list = one.map(List::of).orElseGet(List::of);
            return new PageImpl<>(list, pageable, list.size());
        }

        Page<Order> page = orderRepo.searchByKeyword(keyword, pageable);
        return page.map(OrderMapper::toDTO);
    }

    public boolean isRetryable(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return order.getStatus() == OrderStatus.FAILED || order.getStatus() == OrderStatus.PENDING;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> exportOrders(
            String fromIso,
            String toIso,
            String statusTextVi,
            String keyword
    ) {
        Page<OrderDTO> page = listOrdersFiltered(
                1,
                Integer.MAX_VALUE,
                fromIso,
                toIso,
                statusTextVi,
                keyword
        );
        return page.getContent();
    }

}

