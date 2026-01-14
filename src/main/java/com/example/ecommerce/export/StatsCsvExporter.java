package com.example.ecommerce.export;

import com.example.ecommerce.stats.StatsDTOs.SalesPoint;
import com.example.ecommerce.stats.StatsDTOs.SummaryStats;
import com.example.ecommerce.stats.StatsDTOs.TopProductRow;
import jakarta.servlet.http.HttpServletResponse;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StatsCsvExporter extends AbstractCsvExporter {

    public void exportSummary(SummaryStats s,
                              HttpServletResponse response) throws IOException {
        setResponseHeader(response, "stats-summary_");

        try (ICsvListWriter csv = new CsvListWriter(
                response.getWriter(), CsvPreference.STANDARD_PREFERENCE)) {

            csv.writeHeader(
                    "Tổng doanh thu",
                    "Số đơn",
                    "Sản phẩm đã bán",
                    "Giá trị đơn trung bình (AOV)"
            );
            csv.write(
                    s.revenue(),
                    s.orderCount(),
                    s.itemsSold(),
                    s.aov()
            );
        }
    }

    public void exportSalesTrend(List<SalesPoint> list,
                                 HttpServletResponse response) throws IOException {
        setResponseHeader(response, "stats-sales-trend_");

        try (ICsvListWriter csv = new CsvListWriter(
                response.getWriter(), CsvPreference.STANDARD_PREFERENCE)) {

            csv.writeHeader(
                    "Mốc thời gian",
                    "Doanh thu",
                    "Số đơn",
                    "Sản phẩm đã bán"
            );
            for (SalesPoint p : list) {
                csv.write(
                        p.bucket(),
                        p.revenue(),
                        p.orders(),
                        p.itemsSold()
                );
            }
        }
    }

    public void exportTopProducts(List<TopProductRow> list,
                                  HttpServletResponse response) throws IOException {
        setResponseHeader(response, "stats-top-products_");

        try (ICsvListWriter csv = new CsvListWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                CsvPreference.STANDARD_PREFERENCE)) {

            csv.writeHeader(
                    "ID sản phẩm",
                    "Tên sản phẩm",
                    "Số lượng đã bán",
                    "Doanh thu"
            );
            for (TopProductRow r : list) {
                csv.write(
                        r.productId(),
                        r.name(),
                        r.quantity(),
                        r.revenue()
                );
            }
        }
    }
}
