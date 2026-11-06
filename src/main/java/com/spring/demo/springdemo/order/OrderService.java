package com.spring.demo.springdemo.order;

import com.spring.demo.springdemo.order.dto.CreateOrderRequest;
import com.spring.demo.springdemo.order.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /** Reserves stock and snapshots prices. Fails as a whole if any item is unavailable. */
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(Long id);

    /** @param status optional filter */
    Page<OrderResponse> getOrders(OrderStatus status, Pageable pageable);

    /** Moves the order through its lifecycle; cancelling restocks items and notifies listeners. */
    OrderResponse updateStatus(Long id, OrderStatus status);
}
