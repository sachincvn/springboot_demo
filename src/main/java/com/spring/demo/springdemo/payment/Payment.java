package com.spring.demo.springdemo.payment;

import com.spring.demo.springdemo.common.entity.BaseEntity;
import com.spring.demo.springdemo.common.exception.BusinessException;
import com.spring.demo.springdemo.payment.gateway.GatewayResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = @Index(name = "idx_payments_order_id", columnList = "order_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    /** Plain id, not a @ManyToOne: the payment module talks to orders only through OrderService (module boundary). */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @Column(name = "failure_reason")
    private String failureReason;

    public Payment(Long orderId, BigDecimal amount, PaymentMethod method, GatewayResult result) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = result.status();
        this.transactionRef = result.transactionRef();
        this.failureReason = result.failureReason();
    }

    public void markRefunded() {
        requireStatus(PaymentStatus.SUCCESS, "refund");
        status = PaymentStatus.REFUNDED;
    }

    public void markCancelled() {
        requireStatus(PaymentStatus.PENDING, "cancel");
        status = PaymentStatus.CANCELLED;
    }

    private void requireStatus(PaymentStatus expected, String action) {
        if (status != expected) {
            throw new BusinessException("Cannot %s a payment in status %s".formatted(action, status));
        }
    }
}
