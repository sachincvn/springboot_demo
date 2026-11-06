package com.spring.demo.springdemo.integration.exchangerate;

import com.spring.demo.springdemo.common.currency.CurrencyCode;

import java.math.BigDecimal;

/**
 * Port for any exchange-rate provider. Business code depends on this interface, not on a vendor
 * (Dependency Inversion), so switching provider = writing one new class, no service changes.
 */
public interface ExchangeRateClient {

    /** @return how many units of {@code to} one unit of {@code from} buys */
    BigDecimal getRate(CurrencyCode from, CurrencyCode to);
}
