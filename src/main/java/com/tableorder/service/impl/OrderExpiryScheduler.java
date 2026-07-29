package com.tableorder.service.impl;

import com.tableorder.entity.Order;
import com.tableorder.entity.enums.OrderStatus;
import com.tableorder.entity.enums.PaymentMethod;
import com.tableorder.repository.OrderRepository;
import com.tableorder.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FR-09: order QRIS otomatis EXPIRED jika tidak dibayar dalam batas waktu Midtrans.
 * FR-10: fallback status-check API dipanggil berkala untuk order yang masih
 * PENDING_PAYMENT mendekati/lewat waktu expiry, jaga-jaga webhook gagal diterima.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryScheduler {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @Value("${app.order.pending-payment-expiry-minutes:15}")
    private int expiryMinutes;

    @Scheduled(fixedDelay = 60_000) // tiap 1 menit
    public void checkStalePendingOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(expiryMinutes);
        List<Order> stale = orderRepository.findAllByStatusAndCreatedAtBefore(OrderStatus.PENDING_PAYMENT, threshold);

        for (Order order : stale) {
            if (order.getMetodePembayaran() != PaymentMethod.QRIS) {
                continue; // CASH_COUNTER tidak punya konsep expiry Midtrans
            }
            try {
                // fallback: konfirmasi status terbaru langsung ke Midtrans sebelum dianggap expired
                paymentService.checkStatusToMidtrans(order.getOrderCode());
            } catch (Exception e) {
                log.warn("Gagal cek status Midtrans untuk order {}: {}", order.getOrderCode(), e.getMessage());
            }
        }
    }
}
