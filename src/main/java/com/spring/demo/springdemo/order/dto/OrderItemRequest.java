package com.spring.demo.springdemo.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(

        @Schema(example = "1")
        @NotNull(message = "Product id is required")
        Long productId,

        @Schema(example = "2")
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity must be at most 100 per item")
        Integer quantity) {
}
