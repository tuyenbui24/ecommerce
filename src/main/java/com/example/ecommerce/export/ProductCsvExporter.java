package com.example.ecommerce.export;

import com.example.ecommerce.product.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProductCsvExporter extends AbstractCsvExporter {

    public void export(List<Product> products,
                       HttpServletResponse response) throws IOException {
        setResponseHeader(response, "products_");

        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                CsvPreference.STANDARD_PREFERENCE
        )) {
            String[] csvHeader = {
                    "ID sản phẩm",
                    "Tên sản phẩm",
                    "Giá",
                    "Tổng tồn kho",
                    "Chi tiết size",
                    "Danh mục",
                    "Đang hoạt động"
            };
            String[] fieldMapping = {
                    "id",
                    "name",
                    "price",
                    "totalStock",
                    "sizeDetail",
                    "categoryName",
                    "enabled"
            };
            csvWriter.writeHeader(csvHeader);
            for (Product p : products) {
                csvWriter.write(p, fieldMapping);
            }
        }
    }
}
