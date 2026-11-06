package com.spring.demo.springdemo.payment.gateway;

import com.spring.demo.springdemo.payment.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardPaymentGateway extends SandboxPaymentGateway {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    protected BigDecimal maxAmount() {
        return new BigDecimal("500000");
    }
}
