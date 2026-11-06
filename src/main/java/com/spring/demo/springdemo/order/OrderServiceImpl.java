package com.spring.demo.springdemo.order;

import com.spring.demo.springdemo.common.exception.ResourceNotFoundException;
import com.spring.demo.springdemo.order.dto.CreateOrderRequest;
import com.spring.demo.springdemo.order.dto.OrderItemRequest;
import com.spring.demo.springdemo.order.dto.OrderResponse;
import com.spring.demo.springdemo.order.event.OrderCancelledEvent;
import com.spring.demo.springdemo.product.Product;
import com.spring.demo.springdemo.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates the use case (load, call domain methods, save, publish). The rules themselves live in
 * {@link Order}, {@link Product} and {@link OrderStatus}, which keeps this class short and readable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional // all-or-nothing: if item 3 is out of stock, items 1 and 2 are not reserved either
    public OrderResponse createOrder(CreateOrderRequest request) {
        Map<Long, Product> products = loadProducts(request);

        Order order = new Order(request.customerName(), request.customerEmail());
        for (OrderItemRequest item : request.items()) {
            Product product = products.get(item.productId());
            if (product == null) {
                throw new ResourceNotFoundException(Product.class, item.productId());
            }
            order.addItem(product, item.quantity());
        }

        Order saved = orderRepository.save(order);
        log.info("Created order id={} total={}", saved.getId(), saved.getTotalPrice());
        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        return orderMapper.toResponse(findOrder(id));
    }

    @Override
    public Page<OrderResponse> getOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return page.map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = findOrder(id);
        OrderStatus previous = order.getStatus();
        order.changeStatus(status);

        if (status == OrderStatus.CANCELLED) {
            eventPublisher.publishEvent(new OrderCancelledEvent(id));
        }
        log.info("Order id={} status {} -> {}", id, previous, status);
        return orderMapper.toResponse(order);
    }

    /** One query for all products instead of one per item. */
    private Map<Long, Product> loadProducts(CreateOrderRequest request) {
        var ids = request.items().stream().map(OrderItemRequest::productId).distinct().toList();
        return productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Order.class, id));
    }
}
