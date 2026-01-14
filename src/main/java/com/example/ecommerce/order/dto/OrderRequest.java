package com.example.ecommerce.order.dto;

import com.example.ecommerce.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items;
    private String shippingAddress;
    private String note;
    private PaymentMethod paymentMethod;
    private String phoneNumber;
}
