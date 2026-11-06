package com.spring.demo.springdemo.order;

import com.spring.demo.springdemo.common.entity.BaseEntity;
import com.spring.demo.springdemo.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    // LAZY: don't load the order/product unless we actually read them
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    /** Snapshot of the price when bought; later price changes must not alter old orders. */
    @Column(name = "price_at_purchase", nullable = false)
    private BigDecimal priceAtPurchase;

    /** Package-private: only {@link Order#addItem} creates items. */
    OrderItem(Order order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = product.getPrice();
    }

    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }

    void restoreStock() {
        product.increaseStock(quantity);
    }
}
