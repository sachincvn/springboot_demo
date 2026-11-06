package com.spring.demo.springdemo.payment.gateway;

import com.spring.demo.springdemo.payment.PaymentMethod;

import java.math.BigDecimal;

/**
 * Strategy interface: one implementation per payment method / provider.
 * Adding e.g. a WalletPaymentGateway = one new @Component; PaymentServiceImpl is never edited (Open/Closed).
 * A real Stripe/Razorpay adapter implements this same interface and calls their SDK/HTTP API.
 */
public interface PaymentGateway {

    /** Which {@link PaymentMethod} this gateway handles; used to pick the right strategy. */
    PaymentMethod method();

    GatewayResult charge(Long orderId, BigDecimal amount);

    GatewayResult refund(String transactionRef, BigDecimal amount);
}
