package com.spring.demo.springdemo.order;

import com.spring.demo.springdemo.common.exception.BusinessException;
import com.spring.demo.springdemo.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Plain unit tests of the domain rules: no Spring, no DB, run in milliseconds. */
class OrderTest {

    private final Product keyboard = new Product("Keyboard", null, new BigDecimal("1500.00"), 10);
    private final Product mouse = new Product("Mouse", null, new BigDecimal("499.50"), 5);

    @Test
    void addItemReservesStockAndAccumulatesTotal() {
        Order order = new Order("Sachin", "sachin@example.com");

        order.addItem(keyboard, 2);
        order.addItem(mouse, 1);

        assertThat(order.getTotalPrice()).isEqualByComparingTo("3499.50");
        assertThat(keyboard.getStockQuantity()).isEqualTo(8);
        assertThat(mouse.getStockQuantity()).isEqualTo(4);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void addItemFailsWhenStockIsInsufficient() {
        Order order = new Order("Sachin", "sachin@example.com");

        assertThatThrownBy(() -> order.addItem(mouse, 6))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
        assertThat(mouse.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void priceChangeAfterPurchaseDoesNotAlterOrder() {
        Order order = new Order("Sachin", "sachin@example.com");
        order.addItem(keyboard, 1);

        keyboard.updateDetails("Keyboard", null, new BigDecimal("9999.00"), keyboard.getStockQuantity());

        assertThat(order.getOrderItems().getFirst().getPriceAtPurchase()).isEqualByComparingTo("1500.00");
    }

    @Test
    void illegalTransitionIsRejected() {
        Order order = new Order("Sachin", "sachin@example.com");

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.DELIVERED))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot change order status from PENDING to DELIVERED");
    }

    @Test
    void cancellingRestoresStock() {
        Order order = new Order("Sachin", "sachin@example.com");
        order.addItem(keyboard, 3);

        order.changeStatus(OrderStatus.CANCELLED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(keyboard.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void cancelledOrderCannotBeReopened() {
        Order order = new Order("Sachin", "sachin@example.com");
        order.changeStatus(OrderStatus.CANCELLED);

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.CONFIRMED))
                .isInstanceOf(BusinessException.class);
    }
}
