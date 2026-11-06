package com.spring.demo.springdemo.product.dto;

import com.spring.demo.springdemo.common.currency.CurrencyCode;

import java.math.BigDecimal;

public record PriceQuoteResponse(
        Long productId,
        BigDecimal basePrice,
        CurrencyCode baseCurrency,
        BigDecimal convertedPrice,
        CurrencyCode currency,
        BigDecimal exchangeRate) {
}
