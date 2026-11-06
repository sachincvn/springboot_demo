package com.spring.demo.springdemo.payment.dto;

import com.spring.demo.springdemo.payment.PaymentMethod;
import com.spring.demo.springdemo.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String transactionRef,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
