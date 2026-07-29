package com.tableorder.controller;

import com.tableorder.dto.request.CreateOrderRequest;
import com.tableorder.dto.response.CreateOrderResponse;
import com.tableorder.dto.response.OrderStatusResponse;
import com.tableorder.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint 1 & 7 di API Contract MVP:
 *  POST /api/orders
 *  GET  /api/orders/{order_id}/status
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderStatus(orderId));
    }
}
