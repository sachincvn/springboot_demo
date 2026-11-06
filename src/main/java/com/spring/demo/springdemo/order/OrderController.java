package com.spring.demo.springdemo.order;

import com.spring.demo.springdemo.order.dto.CreateOrderRequest;
import com.spring.demo.springdemo.order.dto.OrderResponse;
import com.spring.demo.springdemo.order.dto.UpdateOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Placing orders and moving them through their lifecycle")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place an order", description = "Reserves stock for every item; fails as a whole if any item is unavailable.")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "404", description = "A product does not exist")
    @ApiResponse(responseCode = "409", description = "Insufficient stock")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    @Operation(summary = "List orders (paged, optional status filter)")
    public Page<OrderResponse> list(
            @Parameter(description = "Only orders in this status") @RequestParam(required = false) OrderStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getOrders(status, pageable);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change order status",
            description = "Allowed: PENDING→CONFIRMED|CANCELLED, CONFIRMED→SHIPPED|CANCELLED, SHIPPED→DELIVERED. "
                    + "Cancelling restocks items and refunds payments.")
    @ApiResponse(responseCode = "409", description = "Transition not allowed")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
