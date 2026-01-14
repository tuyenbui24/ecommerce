package com.example.ecommerce.stats;

import com.example.ecommerce.export.StatsCsvExporter;
import com.example.ecommerce.stats.StatsDTOs.SalesPoint;
import com.example.ecommerce.stats.StatsDTOs.SummaryStats;
import com.example.ecommerce.stats.StatsDTOs.TopProductRow;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
public class StatsAdminController {

    private final StatsService statsService;

    public StatsAdminController(StatsService statsService) {
        this.statsService = statsService;
    }

    private LocalDateTime parse(String iso, boolean start) {
        if (iso == null || iso.isBlank()) {
            return start ? LocalDateTime.now().minusDays(30) : LocalDateTime.now();
        }
        return OffsetDateTime.parse(iso.trim())
                .atZoneSameInstant(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .toLocalDateTime();
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryStats> summary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        LocalDateTime s = parse(from, true);
        LocalDateTime e = parse(to, false).plusDays(1).minusSeconds(1);
        return ResponseEntity.ok(statsService.summary(s, e));
    }

    @GetMapping("/sales-trend")
    public ResponseEntity<List<SalesPoint>> salesTrend(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "daily") String granularity
    ) {
        LocalDateTime s = parse(from, true);
        LocalDateTime e = parse(to, false).plusDays(1).minusSeconds(1);
        return ResponseEntity.ok(statsService.salesTrend(s, e, granularity));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductRow>> topProducts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        LocalDateTime s = parse(from, true);
        LocalDateTime e = parse(to, false).plusDays(1).minusSeconds(1);
        return ResponseEntity.ok(statsService.topProducts(s, e, limit));
    }

    @GetMapping("/export/summary")
    public void exportSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            HttpServletResponse response
    ) throws IOException {
        LocalDateTime s = parse(from, true);
        LocalDateTime e = parse(to, false).plusDays(1).minusSeconds(1);

        SummaryStats stats = statsService.summary(s, e);

        StatsCsvExporter exporter = new StatsCsvExporter();
        exporter.exportSummary(stats, response);
    }

    @GetMapping("/export/sales-trend")
    public void exportSalesTrend(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "daily") String granularity,
            HttpServletResponse response
    ) throws IOException {
        LocalDateTime s = parse(from, true);
        LocalDateTime e = parse(to, false).plusDays(1).minusSeconds(1);

        var list = statsService.salesTrend(s, e, granularity);

        StatsCsvExporter exporter = new StatsCsvExporter();
        exporter.exportSalesTrend(list, response);
    }

    @GetMapping("/export/top-products")
    public void exportTopProducts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletResponse response
    ) throws IOException {
        LocalDateTime s = parse(from, true);
        LocalDateTime e = parse(to, false).plusDays(1).minusSeconds(1);

        var list = statsService.topProducts(s, e, limit);

        StatsCsvExporter exporter = new StatsCsvExporter();
        exporter.exportTopProducts(list, response);
    }

}
