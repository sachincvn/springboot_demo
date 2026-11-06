package com.spring.demo.springdemo.order;

import com.spring.demo.springdemo.common.entity.BaseEntity;
import com.spring.demo.springdemo.common.exception.BusinessException;
import com.spring.demo.springdemo.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root: items are only added through {@link #addItem}, which keeps stock and total in sync.
 * Status only changes through {@link #changeStatus}, which enforces the state machine.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING) // store "PENDING", not 0: reordering the enum can't corrupt data
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(String customerName, String customerEmail) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.status = OrderStatus.PENDING;
        this.totalPrice = BigDecimal.ZERO;
    }

    public void addItem(Product product, int quantity) {
        product.decreaseStock(quantity);
        OrderItem item = new OrderItem(this, product, quantity);
        orderItems.add(item);
        totalPrice = totalPrice.add(item.getSubtotal());
    }

    public void changeStatus(OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new BusinessException("Cannot change order status from %s to %s".formatted(status, next));
        }
        if (next == OrderStatus.CANCELLED) {
            orderItems.forEach(OrderItem::restoreStock);
        }
        status = next;
    }

    /** Read-only view so callers can't bypass {@link #addItem}. */
    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }
}
