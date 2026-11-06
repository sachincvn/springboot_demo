package com.spring.demo.springdemo.payment.gateway;

import com.spring.demo.springdemo.payment.PaymentStatus;

/** Provider-neutral outcome of a gateway call. Static factories make call sites read like English. */
public record GatewayResult(PaymentStatus status, String transactionRef, String failureReason) {

    public static GatewayResult success(String transactionRef) {
        return new GatewayResult(PaymentStatus.SUCCESS, transactionRef, null);
    }

    public static GatewayResult pending(String transactionRef) {
        return new GatewayResult(PaymentStatus.PENDING, transactionRef, null);
    }

    public static GatewayResult refunded(String transactionRef) {
        return new GatewayResult(PaymentStatus.REFUNDED, transactionRef, null);
    }

    public static GatewayResult failed(String reason) {
        return new GatewayResult(PaymentStatus.FAILED, null, reason);
    }
}
