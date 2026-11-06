package com.spring.demo.springdemo.payment;

import com.spring.demo.springdemo.common.exception.BusinessException;
import com.spring.demo.springdemo.order.OrderService;
import com.spring.demo.springdemo.order.OrderStatus;
import com.spring.demo.springdemo.order.dto.OrderResponse;
import com.spring.demo.springdemo.order.event.OrderCancelledEvent;
import com.spring.demo.springdemo.payment.dto.PaymentRequest;
import com.spring.demo.springdemo.payment.dto.PaymentResponse;
import com.spring.demo.springdemo.payment.gateway.CardPaymentGateway;
import com.spring.demo.springdemo.payment.gateway.CashOnDeliveryGateway;
import com.spring.demo.springdemo.payment.gateway.GatewayResult;
import com.spring.demo.springdemo.payment.gateway.UpiPaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Service logic with collaborators mocked; real gateway strategies so the selection logic is exercised. */
class PaymentServiceImplTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OrderService orderService = mock(OrderService.class);
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        paymentService = new PaymentServiceImpl(
                List.of(new CardPaymentGateway(), new UpiPaymentGateway(), new CashOnDeliveryGateway()),
                paymentRepository, new PaymentMapper(), orderService);
    }

    @Test
    void successfulUpiPaymentConfirmsOrder() {
        givenOrder(1L, OrderStatus.PENDING, "2500.00");

        PaymentResponse payment = paymentService.pay(new PaymentRequest(1L, PaymentMethod.UPI));

        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.transactionRef()).startsWith("upi_");
        verify(orderService).updateStatus(1L, OrderStatus.CONFIRMED);
    }

    @Test
    void upiOverLimitIsRecordedAsFailedAndOrderStaysPending() {
        givenOrder(1L, OrderStatus.PENDING, "150000.00");

        PaymentResponse payment = paymentService.pay(new PaymentRequest(1L, PaymentMethod.UPI));

        assertThat(payment.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.failureReason()).contains("limited to");
        verify(orderService, never()).updateStatus(any(), any());
    }

    @Test
    void cashOnDeliveryStaysPendingButConfirmsOrder() {
        givenOrder(1L, OrderStatus.PENDING, "800.00");

        PaymentResponse payment = paymentService.pay(new PaymentRequest(1L, PaymentMethod.CASH_ON_DELIVERY));

        assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);
        verify(orderService).updateStatus(1L, OrderStatus.CONFIRMED);
    }

    @Test
    void nonPendingOrderCannotBePaid() {
        givenOrder(1L, OrderStatus.CONFIRMED, "800.00");

        assertThatThrownBy(() -> paymentService.pay(new PaymentRequest(1L, PaymentMethod.CARD)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PENDING orders");
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void cancellingOrderRefundsSuccessfulAndCancelsPendingPayments() {
        Payment paid = new Payment(1L, new BigDecimal("100"), PaymentMethod.CARD, GatewayResult.success("card_1"));
        Payment cod = new Payment(1L, new BigDecimal("100"), PaymentMethod.CASH_ON_DELIVERY, GatewayResult.pending("cod_1"));
        Payment failed = new Payment(1L, new BigDecimal("100"), PaymentMethod.UPI, GatewayResult.failed("declined"));
        when(paymentRepository.findByOrderIdOrderByIdDesc(1L)).thenReturn(List.of(paid, cod, failed));

        paymentService.onOrderCancelled(new OrderCancelledEvent(1L));

        assertThat(paid.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(cod.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(failed.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    private void givenOrder(Long id, OrderStatus status, String total) {
        when(orderService.getOrder(id)).thenReturn(new OrderResponse(
                id, "Sachin", "sachin@example.com", status, new BigDecimal(total), List.of(), null, null));
    }
}
