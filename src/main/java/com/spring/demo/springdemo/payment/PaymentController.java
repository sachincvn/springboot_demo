package com.spring.demo.springdemo.payment;

import com.spring.demo.springdemo.payment.dto.PaymentRequest;
import com.spring.demo.springdemo.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Paying for orders through pluggable payment gateways")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Pay for an order",
            description = "CARD (limit 5,00,000), UPI (limit 1,00,000) succeed immediately; CASH_ON_DELIVERY "
                    + "(limit 50,000) stays PENDING. Over the limit → payment is recorded as FAILED.")
    @ApiResponse(responseCode = "201", description = "Payment attempt recorded (check its status)")
    @ApiResponse(responseCode = "409", description = "Order is not PENDING")
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.pay(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/payments/{id}").buildAndExpand(payment.id()).toUri();
        return ResponseEntity.created(location).body(payment);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment by id")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public PaymentResponse get(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    @GetMapping
    @Operation(summary = "List all payment attempts for an order (newest first)")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public List<PaymentResponse> listForOrder(@RequestParam Long orderId) {
        return paymentService.getPaymentsForOrder(orderId);
    }
}
