package com.spring.demo.springdemo.order;

/**
 * Order lifecycle as a state machine. The enum owns its own rules, so "can PENDING go to DELIVERED?"
 * is answered in exactly one place instead of if-chains scattered across services.
 *
 * PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
 *    \___________\______-> CANCELLED
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
