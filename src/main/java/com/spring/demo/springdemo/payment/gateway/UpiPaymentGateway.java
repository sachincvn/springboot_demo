package com.spring.demo.springdemo.payment.gateway;

import com.spring.demo.springdemo.payment.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UpiPaymentGateway extends SandboxPaymentGateway {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }

    /** Standard NPCI per-transaction UPI limit. */
    @Override
    protected BigDecimal maxAmount() {
        return new BigDecimal("100000");
    }
}
