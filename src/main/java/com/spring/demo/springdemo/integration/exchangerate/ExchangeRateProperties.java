package com.spring.demo.springdemo.integration.exchangerate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** Typed settings bound from {@code integration.exchange-rate.*} in application.yaml. App fails fast if missing. */
@Validated
@ConfigurationProperties(prefix = "integration.exchange-rate")
public record ExchangeRateProperties(
        @NotBlank String baseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout) {
}
