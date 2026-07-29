package com.tableorder.controller;

import com.tableorder.dto.request.ConfirmPaymentRequest;
import com.tableorder.dto.request.MarkSyncedRequest;
import com.tableorder.dto.request.UpdateOrderStatusRequest;
import com.tableorder.dto.response.CashierOrderListResponse;
import com.tableorder.dto.response.ConfirmPaymentResponse;
import com.tableorder.dto.response.MarkSyncedResponse;
import com.tableorder.dto.response.UpdateStatusResponse;
import com.tableorder.service.CashierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint 3-6 di API Contract MVP - Companion Screen (Kasir):
 *  GET   /api/cashier/orders?status=active
 *  POST  /api/cashier/orders/{order_id}/confirm-payment
 *  POST  /api/cashier/orders/{order_id}/mark-synced
 *  PATCH /api/cashier/orders/{order_id}/status
 */
@RestController
@RequestMapping("/api/cashier/orders")
@RequiredArgsConstructor
public class CashierController {

    private final CashierService cashierService;

    @GetMapping
    public ResponseEntity<CashierOrderListResponse> listOrders(
            @RequestParam(value = "status", required = false, defaultValue = "active") String status) {
        // MVP: hanya mendukung "active" (order yang belum SELESAI/EXPIRED/FAILED).
        return ResponseEntity.ok(cashierService.listActiveOrders());
    }

    @PostMapping("/{orderId}/confirm-payment")
    public ResponseEntity<ConfirmPaymentResponse> confirmPayment(
            @PathVariable String orderId,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(cashierService.confirmPayment(orderId, request.getConfirmedBy()));
    }

    @PostMapping("/{orderId}/mark-synced")
    public ResponseEntity<MarkSyncedResponse> markSynced(
            @PathVariable String orderId,
            @Valid @RequestBody MarkSyncedRequest request) {
        return ResponseEntity.ok(cashierService.markSynced(orderId, request.getMarkedBy()));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<UpdateStatusResponse> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(cashierService.updateStatus(orderId, request.getStatus()));
    }
}
