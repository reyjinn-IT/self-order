package com.tableorder.service.impl;

import com.tableorder.config.MidtransProperties;
import com.tableorder.dto.request.MidtransNotificationRequest;
import com.tableorder.dto.response.PaymentSnapInfo;
import com.tableorder.entity.Order;
import com.tableorder.entity.Payment;
import com.tableorder.entity.enums.OrderStatus;
import com.tableorder.entity.enums.PaymentStatus;
import com.tableorder.exception.InvalidSignatureException;
import com.tableorder.exception.OrderNotFoundException;
import com.tableorder.repository.OrderRepository;
import com.tableorder.repository.PaymentRepository;
import com.tableorder.service.PaymentService;
import com.tableorder.util.MidtransSignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MidtransProperties midtransProperties;
    private final MidtransSignatureValidator signatureValidator;
    private final WebClient midtransWebClient;

    private static final DateTimeFormatter EXPIRY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PaymentSnapInfo createSnapTransaction(Order order) {
        Payment payment = order.getPayment();

        Map<String, Object> requestBody = Map.of(
                "transaction_details", Map.of(
                        "order_id", order.getOrderCode(),
                        "gross_amount", order.getTotalHarga().longValueExact()
                ),
                "enabled_payments", List.of("qris"),
                "expiry", Map.of(
                        "unit", "minute",
                        "duration", 15
                )
        );

        Map<String, Object> response = midtransWebClient.post()
                .uri(midtransProperties.getBaseUrl() + "/snap/v1/transactions")
                .headers(headers -> applyBasicAuth(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String snapToken = response != null ? (String) response.get("token") : null;
        String redirectUrl = response != null ? (String) response.get("redirect_url") : null;
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

        payment.setMidtransSnapToken(snapToken);
        payment.setExpiryTime(expiryTime);
        paymentRepository.save(payment);

        return PaymentSnapInfo.builder()
                .snapToken(snapToken)
                .snapRedirectUrl(redirectUrl)
                .expiryTime(expiryTime)
                .build();
    }

    @Override
    public void handleNotification(MidtransNotificationRequest notification) {
        boolean valid = signatureValidator.isValid(
                notification.getOrderId(),
                notification.getStatusCode(),
                notification.getGrossAmount(),
                midtransProperties.getServerKey(),
                notification.getSignatureKey()
        );
        if (!valid) {
            log.warn("Signature Midtrans tidak valid untuk order_id={}", notification.getOrderId());
            throw new InvalidSignatureException();
        }

        applyTransactionStatus(notification.getOrderId(), notification.getTransactionStatus(),
                notification.getFraudStatus(), notification.getTransactionId());
    }

    @Override
    public void checkStatusToMidtrans(String orderCode) {
        Map<String, Object> response = midtransWebClient.get()
                .uri(midtransProperties.getApiBaseUrl() + "/v2/" + orderCode + "/status")
                .headers(this::applyBasicAuth)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            log.warn("Tidak ada response dari Midtrans status API untuk order_code={}", orderCode);
            return;
        }

        String transactionStatus = (String) response.get("transaction_status");
        String fraudStatus = (String) response.get("fraud_status");
        String transactionId = (String) response.get("transaction_id");

        applyTransactionStatus(orderCode, transactionStatus, fraudStatus, transactionId);
    }

    private void applyTransactionStatus(String orderCode, String transactionStatus,
                                         String fraudStatus, String transactionId) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));
        Payment payment = order.getPayment();
        if (payment == null) {
            log.warn("Order {} tidak punya data payment (bukan QRIS?)", orderCode);
            return;
        }

        // Sesuai PRD Bagian 7: mapping status Midtrans -> status internal.
        switch (transactionStatus) {
            case "settlement" -> markPaid(order, payment, transactionId);
            case "capture" -> {
                if ("accept".equalsIgnoreCase(fraudStatus)) {
                    markPaid(order, payment, transactionId);
                }
            }
            case "expire" -> {
                order.setStatus(OrderStatus.EXPIRED);
                payment.setStatus(PaymentStatus.EXPIRED);
            }
            case "deny", "cancel" -> {
                order.setStatus(OrderStatus.FAILED);
                payment.setStatus(PaymentStatus.FAILED);
            }
            case "pending" -> {
                // tetap PENDING_PAYMENT, tidak ada perubahan
            }
            default -> log.info("transaction_status '{}' tidak dikenali untuk order {}", transactionStatus, orderCode);
        }

        payment.setMidtransTransactionId(transactionId);
        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    private void markPaid(Order order, Payment payment, String transactionId) {
        if (order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.DIPROSES
                || order.getStatus() == OrderStatus.SIAP
                || order.getStatus() == OrderStatus.SELESAI) {
            // idempotent: sudah PAID sebelumnya, jangan timpa paid_at
            return;
        }
        order.setStatus(OrderStatus.PAID);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setMidtransTransactionId(transactionId);
    }

    private void applyBasicAuth(HttpHeaders headers) {
        String basicAuth = Base64.getEncoder().encodeToString(
                (midtransProperties.getServerKey() + ":").getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
}
