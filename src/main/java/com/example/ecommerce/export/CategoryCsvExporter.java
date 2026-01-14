package com.example.ecommerce.export;

import com.example.ecommerce.category.entity.Category;
import jakarta.servlet.http.HttpServletResponse;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CategoryCsvExporter extends AbstractCsvExporter {

    public void export(List<Category> categories,
                       HttpServletResponse response) throws IOException {

        setResponseHeader(response, "categories_");

        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                CsvPreference.STANDARD_PREFERENCE
        )) {
            String[] csvHeader = {
                    "ID danh mục",
                    "Tên danh mục",
                    "Đường dẫn (slug)"
            };

            String[] fieldMapping = {
                    "id",
                    "name",
                    "slug"
            };

            csvWriter.writeHeader(csvHeader);

            for (Category c : categories) {
                csvWriter.write(c, fieldMapping);
            }
        }
    }
}
