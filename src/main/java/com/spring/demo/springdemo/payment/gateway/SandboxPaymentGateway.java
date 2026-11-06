package com.spring.demo.springdemo.payment.gateway;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Template Method base for simulated providers: the charge flow (limit check -> reference -> result)
 * is written once here; subclasses only fill in what differs.
 *
 * shortcut: no real money moves; replace a subclass with an adapter for a real provider (Stripe, Razorpay)
 * when you get sandbox API keys. Nothing else in the app changes.
 */
public abstract class SandboxPaymentGateway implements PaymentGateway {

    @Override
    public final GatewayResult charge(Long orderId, BigDecimal amount) {
        if (amount.compareTo(maxAmount()) > 0) {
            return GatewayResult.failed("%s payments are limited to %s INR".formatted(method(), maxAmount()));
        }
        return onChargeAccepted(newReference());
    }

    @Override
    public GatewayResult refund(String transactionRef, BigDecimal amount) {
        return GatewayResult.refunded(transactionRef);
    }

    /** Largest single payment this method allows. */
    protected abstract BigDecimal maxAmount();

    /** Default: money collected immediately. */
    protected GatewayResult onChargeAccepted(String transactionRef) {
        return GatewayResult.success(transactionRef);
    }

    private String newReference() {
        return method().name().toLowerCase() + "_" + UUID.randomUUID();
    }
}
