package com.spring.demo.springdemo.payment;

import com.spring.demo.springdemo.common.exception.BusinessException;
import com.spring.demo.springdemo.common.exception.ResourceNotFoundException;
import com.spring.demo.springdemo.order.OrderService;
import com.spring.demo.springdemo.order.OrderStatus;
import com.spring.demo.springdemo.order.dto.OrderResponse;
import com.spring.demo.springdemo.order.event.OrderCancelledEvent;
import com.spring.demo.springdemo.payment.dto.PaymentRequest;
import com.spring.demo.springdemo.payment.dto.PaymentResponse;
import com.spring.demo.springdemo.payment.gateway.GatewayResult;
import com.spring.demo.springdemo.payment.gateway.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final Map<PaymentMethod, PaymentGateway> gateways = new EnumMap<>(PaymentMethod.class);
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;

    /** Spring injects every PaymentGateway bean; we index them by method once at startup. */
    public PaymentServiceImpl(List<PaymentGateway> gateways, PaymentRepository paymentRepository,
                              PaymentMapper paymentMapper, OrderService orderService) {
        gateways.forEach(gateway -> {
            if (this.gateways.put(gateway.method(), gateway) != null) {
                throw new IllegalStateException("Two payment gateways registered for " + gateway.method());
            }
        });
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.orderService = orderService;
    }

    // shortcut: the gateway call runs inside the DB transaction; with a real provider, call it outside
    // the transaction (or via an outbox) so a slow provider doesn't hold a DB connection.
    @Override
    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        OrderResponse order = orderService.getOrder(request.orderId());
        if (order.status() != OrderStatus.PENDING) {
            throw new BusinessException("Only PENDING orders can be paid; order %d is %s"
                    .formatted(order.id(), order.status()));
        }

        GatewayResult result = gatewayFor(request.method()).charge(order.id(), order.totalPrice());
        Payment payment = paymentRepository.save(new Payment(order.id(), order.totalPrice(), request.method(), result));

        if (payment.getStatus().isActive()) {
            orderService.updateStatus(order.id(), OrderStatus.CONFIRMED);
        }
        log.info("Payment id={} order={} method={} status={}",
                payment.getId(), order.id(), payment.getMethod(), payment.getStatus());
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getPayment(Long id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(Payment.class, id));
    }

    @Override
    public List<PaymentResponse> getPaymentsForOrder(Long orderId) {
        orderService.getOrder(orderId); // 404 for unknown orders instead of an ambiguous empty list
        return paymentMapper.toResponses(paymentRepository.findByOrderIdOrderByIdDesc(orderId));
    }

    /** Runs in the cancelling transaction: if a refund fails, the cancellation rolls back too. */
    @EventListener
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        for (Payment payment : paymentRepository.findByOrderIdOrderByIdDesc(event.orderId())) {
            switch (payment.getStatus()) {
                case SUCCESS -> {
                    GatewayResult refund = gatewayFor(payment.getMethod())
                            .refund(payment.getTransactionRef(), payment.getAmount());
                    if (refund.status() != PaymentStatus.REFUNDED) {
                        throw new BusinessException("Refund failed for payment " + payment.getId());
                    }
                    payment.markRefunded();
                    log.info("Refunded payment id={} for cancelled order={}", payment.getId(), event.orderId());
                }
                case PENDING -> payment.markCancelled();
                default -> { /* FAILED / already refunded / cancelled: nothing to undo */ }
            }
        }
    }

    private PaymentGateway gatewayFor(PaymentMethod method) {
        PaymentGateway gateway = gateways.get(method);
        if (gateway == null) {
            throw new BusinessException("Payment method %s is not available".formatted(method));
        }
        return gateway;
    }
}
