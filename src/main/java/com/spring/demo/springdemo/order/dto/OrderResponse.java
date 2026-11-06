package com.spring.demo.springdemo.order.dto;

import com.spring.demo.springdemo.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
