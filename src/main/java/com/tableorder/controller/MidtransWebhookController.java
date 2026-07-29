package com.tableorder.controller;

import com.tableorder.dto.request.MidtransNotificationRequest;
import com.tableorder.exception.InvalidSignatureException;
import com.tableorder.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint 2 di API Contract MVP: POST /api/payments/midtrans/notification
 *
 * Selalu balas 200 OK ke Midtrans kecuali signature tidak valid (403),
 * supaya Midtrans tidak retry terus-menerus (FR-08).
 */
@RestController
@RequestMapping("/api/payments/midtrans")
@RequiredArgsConstructor
public class MidtransWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/notification")
    public ResponseEntity<Map<String, String>> handleNotification(@RequestBody MidtransNotificationRequest notification) {
        try {
            paymentService.handleNotification(notification);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (InvalidSignatureException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("status", "invalid_signature"));
        }
    }
}
