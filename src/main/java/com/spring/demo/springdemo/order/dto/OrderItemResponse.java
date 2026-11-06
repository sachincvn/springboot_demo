package com.spring.demo.springdemo.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtPurchase,
        BigDecimal subtotal) {
}
