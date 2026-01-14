package com.example.ecommerce.stats;

import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.stats.StatsDTOs.SalesPoint;
import com.example.ecommerce.stats.StatsDTOs.SummaryStats;
import com.example.ecommerce.stats.StatsDTOs.TopProductRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final OrderRepository orderRepo;

    public StatsService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    private static final List<OrderStatus> OK_STATUSES = List.of(
            OrderStatus.PAID,
            OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING,
            OrderStatus.SHIPPED,
            OrderStatus.COMPLETED
    );

    private static BigDecimal bd(long v) { return BigDecimal.valueOf(v); }
    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    @Transactional(readOnly = true)
    public SummaryStats summary(LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderRepo.findByStatusInAndOrderTimeBetween(OK_STATUSES, from, to);

        BigDecimal revenue = orders.stream()
                .map(o -> nz(o.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderCount = orders.size();

        long itemsSold = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .mapToLong(it -> it.getQuantity() == null ? 0 : it.getQuantity())
                .sum();

        BigDecimal aov = orderCount == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(orderCount), 0, RoundingMode.HALF_UP);

        return new SummaryStats(revenue, orderCount, itemsSold, aov);
    }

    @Transactional(readOnly = true)
    public List<SalesPoint> salesTrend(LocalDateTime from, LocalDateTime to, String granularity) {
        String g = (granularity == null ? "daily" : granularity.trim().toLowerCase());
        List<Order> orders = orderRepo.findByStatusInAndOrderTimeBetween(OK_STATUSES, from, to);

        Map<String, BucketAcc> acc = new LinkedHashMap<>();

        for (Order o : orders) {
            String key = switch (g) {
                case "weekly" -> {
                    LocalDate d = o.getOrderTime().toLocalDate();
                    java.time.temporal.WeekFields wf = java.time.temporal.WeekFields.ISO;
                    int w = d.get(wf.weekOfWeekBasedYear());
                    yield d.getYear() + "-W" + String.format("%02d", w);
                }
                case "monthly" -> {
                    YearMonth ym = YearMonth.from(o.getOrderTime());
                    yield ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                }
                default -> o.getOrderTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            };

            BucketAcc b = acc.computeIfAbsent(key, k -> new BucketAcc());
            b.revenue = b.revenue.add(nz(o.getTotalPrice()));
            b.orders++;

            for (OrderItem it : o.getItems()) {
                b.itemsSold += (it.getQuantity() == null ? 0 : it.getQuantity());
            }
        }

        switch (g) {
            case "monthly" -> {
                YearMonth start = YearMonth.from(from);
                YearMonth end = YearMonth.from(to);
                YearMonth cur = start;
                while (!cur.isAfter(end)) {
                    String k = cur.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    acc.putIfAbsent(k, new BucketAcc());
                    cur = cur.plusMonths(1);
                }
            }
            case "weekly" -> {
                LocalDate d = from.toLocalDate();
                java.time.temporal.WeekFields wf = java.time.temporal.WeekFields.ISO;
                int endWeek = to.toLocalDate().get(wf.weekOfWeekBasedYear());
                int endYear = to.getYear();
                while (true) {
                    int w = d.get(wf.weekOfWeekBasedYear());
                    int y = d.getYear();
                    String k = y + "-W" + String.format("%02d", w);
                    acc.putIfAbsent(k, new BucketAcc());
                    if (y == endYear && w >= endWeek) break;
                    d = d.plusWeeks(1);
                }
            }
            default -> {
                LocalDate d = from.toLocalDate();
                LocalDate end = to.toLocalDate();
                while (!d.isAfter(end)) {
                    String k = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    acc.putIfAbsent(k, new BucketAcc());
                    d = d.plusDays(1);
                }
            }
        }

        return acc.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new SalesPoint(
                        e.getKey(),
                        e.getValue().revenue,
                        e.getValue().orders,
                        e.getValue().itemsSold
                ))
                .toList();
    }

    private static class BucketAcc {
        BigDecimal revenue = BigDecimal.ZERO;
        long orders = 0L;
        long itemsSold = 0L;
    }

    @Transactional(readOnly = true)
    public List<TopProductRow> topProducts(LocalDateTime from, LocalDateTime to, int limit) {
        List<Order> orders = orderRepo.findByStatusInAndOrderTimeBetween(OK_STATUSES, from, to);

        Map<Integer, TopAcc> map = new HashMap<>();

        for (Order o : orders) {
            for (OrderItem it : o.getItems()) {
                Integer id = it.getProduct().getId();
                String name = it.getProduct().getName();
                long qty = it.getQuantity() == null ? 0 : it.getQuantity();
                BigDecimal line = nz(it.getPrice()).multiply(BigDecimal.valueOf(qty));

                TopAcc acc = map.computeIfAbsent(id, k -> new TopAcc(name));
                acc.quantity += qty;
                acc.revenue = acc.revenue.add(line);
            }
        }

        return map.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().quantity, a.getValue().quantity))
                .limit(Math.max(limit, 1))
                .map(e -> new TopProductRow(
                        e.getKey(), e.getValue().name, e.getValue().quantity, e.getValue().revenue
                )).toList();
    }

    private static class TopAcc {
        String name;
        long quantity = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        TopAcc(String name) { this.name = name; }
    }
}
