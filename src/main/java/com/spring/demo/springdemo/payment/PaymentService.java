package com.spring.demo.springdemo.payment;

import com.spring.demo.springdemo.payment.dto.PaymentRequest;
import com.spring.demo.springdemo.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {

    /**
     * Charges a PENDING order through the gateway for the chosen method.
     * On success (or cash-on-delivery) the order becomes CONFIRMED; a declined payment is recorded as FAILED.
     */
    PaymentResponse pay(PaymentRequest request);

    PaymentResponse getPayment(Long id);

    List<PaymentResponse> getPaymentsForOrder(Long orderId);
}
