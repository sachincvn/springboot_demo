package com.spring.demo.springdemo.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Input for create and update. Validation lives on the DTO: the API boundary is where untrusted data enters. */
public record ProductRequest(

        @Schema(example = "Mechanical Keyboard")
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @Schema(example = "RGB backlit, hot-swappable switches")
        @Size(max = 255, message = "Description must be at most 255 characters")
        String description,

        @Schema(example = "4999.00", description = "Price in INR")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price can have at most 2 decimal places")
        BigDecimal price,

        @Schema(example = "25")
        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity cannot be negative")
        Integer stockQuantity) {
}
