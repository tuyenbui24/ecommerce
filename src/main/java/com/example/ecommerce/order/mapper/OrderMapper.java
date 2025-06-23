package com.example.ecommerce.order.mapper;

import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.dto.OrderItemDTO;
import com.example.ecommerce.order.entity.Order;

public class OrderMapper {

    public static OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderTime(order.getOrderTime());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().name());
        dto.setNote(order.getNote());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setUserFullName(order.getUser() != null ? order.getUser().getFullName() : "Khách");
        dto.setItems(order.getItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setPrice(item.getPrice());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setImage(item.getProduct().getImage());
                    itemDTO.setProductId(item.getProduct().getId());
                    return itemDTO;
                }).toList());
        return dto;
    }
}

