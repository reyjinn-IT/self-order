package com.tableorder.service;

import com.tableorder.dto.request.CreateOrderRequest;
import com.tableorder.dto.response.CreateOrderResponse;
import com.tableorder.dto.response.OrderStatusResponse;

public interface OrderService {
    CreateOrderResponse createOrder(CreateOrderRequest request);
    OrderStatusResponse getOrderStatus(String orderCode);
}
