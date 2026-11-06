package com.spring.demo.springdemo.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateOrderRequest(

        @Schema(example = "Sachin")
        @NotBlank(message = "Customer name is required")
        @Size(max = 255)
        String customerName,

        @Schema(example = "sachin@example.com")
        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        @Size(max = 255)
        String customerEmail,

        @NotEmpty(message = "Order must contain at least one item")
        @Size(max = 50, message = "Order can contain at most 50 items")
        List<@Valid @NotNull OrderItemRequest> items) {
}
