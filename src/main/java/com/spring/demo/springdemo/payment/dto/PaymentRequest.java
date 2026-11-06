package com.spring.demo.springdemo.payment.dto;

import com.spring.demo.springdemo.payment.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @Schema(example = "1")
        @NotNull(message = "Order id is required")
        Long orderId,

        @Schema(example = "UPI")
        @NotNull(message = "Payment method is required")
        PaymentMethod method) {
}
