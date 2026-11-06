package com.spring.demo.springdemo.order;

import com.spring.demo.springdemo.common.mapper.ResponseMapper;
import com.spring.demo.springdemo.order.dto.OrderItemResponse;
import com.spring.demo.springdemo.order.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper implements ResponseMapper<Order, OrderResponse> {

    @Override
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getOrderItems().stream().map(this::toItemResponse).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceAtPurchase(),
                item.getSubtotal());
    }
}
