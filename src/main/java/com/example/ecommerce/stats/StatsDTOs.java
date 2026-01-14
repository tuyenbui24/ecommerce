package com.example.ecommerce.stats;

import java.math.BigDecimal;

public class StatsDTOs {

    public record SummaryStats(
            BigDecimal revenue,
            long orderCount,
            long itemsSold,
            BigDecimal aov
    ) {}

    public record SalesPoint(
            String bucket,
            BigDecimal revenue,
            long orders,
            long itemsSold
    ) {}

    public record TopProductRow(
            Integer productId,
            String name,
            long quantity,
            BigDecimal revenue
    ) {}
}
