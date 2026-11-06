package com.spring.demo.springdemo.product;

import com.spring.demo.springdemo.common.entity.BaseEntity;
import com.spring.demo.springdemo.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Rich domain entity: state changes go through methods that enforce the rules (encapsulation),
 * so no caller can ever set stock to a negative number. No public setters on purpose.
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA, hidden from everyone else
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    /** Price in the store currency (INR). */
    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    /** Optimistic locking: two orders buying the last item at once -> one gets HTTP 409 instead of overselling. */
    @Version
    private Long version;

    public Product(String name, String description, BigDecimal price, int stockQuantity) {
        updateDetails(name, description, price, stockQuantity);
    }

    public void updateDetails(String name, String description, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new BusinessException("Insufficient stock for '%s': requested %d, available %d"
                    .formatted(name, quantity, stockQuantity));
        }
        stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        stockQuantity += quantity;
    }
}
