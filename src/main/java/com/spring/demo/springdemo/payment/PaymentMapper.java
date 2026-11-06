package com.spring.demo.springdemo.payment;

import com.spring.demo.springdemo.common.mapper.ResponseMapper;
import com.spring.demo.springdemo.payment.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper implements ResponseMapper<Payment, PaymentResponse> {

    @Override
    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getTransactionRef(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
