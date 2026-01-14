package com.example.ecommerce.order.dto;

import com.example.ecommerce.order.entity.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Integer id;
    private LocalDateTime orderTime;
    private BigDecimal totalPrice;
    private String status;
    private String statusLabel;
    private String note;
    private String shippingAddress;
    private String phoneNumber;
    private PaymentMethod paymentMethod;
    private List<OrderItemDTO> items;
    private String userFullName;
    private String gatewayTxnNo;
    private LocalDateTime paidAt;
}

