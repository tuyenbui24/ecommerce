package com.example.ecommerce.export;

import com.example.ecommerce.order.dto.OrderDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OrderCsvExporter extends AbstractCsvExporter {

    public void export(List<OrderDTO> orders,
                       HttpServletResponse response) throws IOException {

        setResponseHeader(response, "orders_");

        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8),
                CsvPreference.STANDARD_PREFERENCE
        )) {
            String[] csvHeader = {
                    "ID đơn hàng",
                    "Thời gian đặt",
                    "Khách hàng",
                    "Số điện thoại",
                    "Địa chỉ",
                    "Ghi chú",
                    "Tổng tiền",
                    "Trạng thái",
                    "Phương thức thanh toán"
            };

            String[] fieldMapping = {
                    "id",
                    "orderTime",
                    "userFullName",
                    "phoneNumber",
                    "shippingAddress",
                    "note",
                    "totalPrice",
                    "statusLabel",
                    "paymentMethod"
            };

            csvWriter.writeHeader(csvHeader);

            for (OrderDTO o : orders) {
                csvWriter.write(o, fieldMapping);
            }
        }
    }
}
