package com.spring.demo.springdemo.order.event;

/**
 * Published when an order is cancelled. Other features (payment refunds, emails, ...) listen to it,
 * so the order module never needs to know they exist (loose coupling, Open/Closed).
 */
public record OrderCancelledEvent(Long orderId) {
}
