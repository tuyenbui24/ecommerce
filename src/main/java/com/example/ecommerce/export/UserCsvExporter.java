package com.example.ecommerce.export;

import com.example.ecommerce.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UserCsvExporter extends AbstractCsvExporter {

    public void export(List<User> users,
                       HttpServletResponse response) throws IOException {

        setResponseHeader(response, "users_");

        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                CsvPreference.STANDARD_PREFERENCE
        )) {
            String[] csvHeader = {
                    "ID người dùng",
                    "Email",
                    "Tên",
                    "Họ",
                    "Vai trò"
            };

            String[] fieldMapping = {
                    "id",
                    "email",
                    "firstName",
                    "lastName",
                    "roles"
            };

            csvWriter.writeHeader(csvHeader);

            for (User u : users) {
                csvWriter.write(u, fieldMapping);
            }
        }
    }
}
