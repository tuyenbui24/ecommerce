package com.example.ecommerce.export;

import com.example.ecommerce.staff.entity.Staff;
import jakarta.servlet.http.HttpServletResponse;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StaffCsvExporter extends AbstractCsvExporter {

    public void export(List<Staff> staffs,
                       HttpServletResponse response) throws IOException {

        setResponseHeader(response, "staffs_");

        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                CsvPreference.STANDARD_PREFERENCE
        )) {
            String[] csvHeader = {
                    "ID nhân viên",
                    "Email",
                    "Tên",
                    "Họ",
                    "Trạng thái",
                    "Vai trò"
            };

            String[] fieldMapping = {
                    "id",
                    "email",
                    "firstName",
                    "lastName",
                    "enabled",
                    "roles"
            };

            csvWriter.writeHeader(csvHeader);

            for (Staff s : staffs) {
                csvWriter.write(s, fieldMapping);
            }
        }
    }
}
