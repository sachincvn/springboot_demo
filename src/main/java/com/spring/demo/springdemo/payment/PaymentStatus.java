package com.spring.demo.springdemo.payment;

public enum PaymentStatus {
    /** Money not collected yet (e.g. cash on delivery). */
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED,
    /** A pending payment that will never be collected because the order was cancelled. */
    CANCELLED;

    /** Payments that count towards an order being paid or on its way to being paid. */
    public boolean isActive() {
        return this == PENDING || this == SUCCESS;
    }
}
