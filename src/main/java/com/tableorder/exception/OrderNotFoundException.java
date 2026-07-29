package com.tableorder.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends ApiException {
    public OrderNotFoundException(String orderId) {
        super(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND",
                "Order " + orderId + " tidak ditemukan");
    }
}
