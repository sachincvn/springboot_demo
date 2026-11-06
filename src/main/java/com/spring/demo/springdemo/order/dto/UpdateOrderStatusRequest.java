package com.spring.demo.springdemo.order.dto;

import com.spring.demo.springdemo.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @Schema(example = "SHIPPED")
        @NotNull(message = "Status is required")
        OrderStatus status) {
}
