package com.spring.demo.springdemo.payment.gateway;

import com.spring.demo.springdemo.payment.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CashOnDeliveryGateway extends SandboxPaymentGateway {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CASH_ON_DELIVERY;
    }

    @Override
    protected BigDecimal maxAmount() {
        return new BigDecimal("50000");
    }

    /** Cash is collected by the courier later, so the payment stays PENDING. */
    @Override
    protected GatewayResult onChargeAccepted(String transactionRef) {
        return GatewayResult.pending(transactionRef);
    }
}
