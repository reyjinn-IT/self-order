package com.tableorder.service.impl;

import com.tableorder.dto.response.CashierOrderItemView;
import com.tableorder.dto.response.CashierOrderListResponse;
import com.tableorder.dto.response.CashierOrderView;
import com.tableorder.dto.response.ConfirmPaymentResponse;
import com.tableorder.dto.response.MarkSyncedResponse;
import com.tableorder.dto.response.UpdateStatusResponse;
import com.tableorder.entity.CashierLog;
import com.tableorder.entity.Order;
import com.tableorder.entity.OrderItem;
import com.tableorder.entity.enums.CashierAction;
import com.tableorder.entity.enums.OrderStatus;
import com.tableorder.entity.enums.PaymentStatus;
import com.tableorder.exception.AlreadyPaidException;
import com.tableorder.exception.InvalidStatusTransitionException;
import com.tableorder.exception.OrderNotFoundException;
import com.tableorder.repository.CashierLogRepository;
import com.tableorder.repository.OrderRepository;
import com.tableorder.service.CashierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CashierServiceImpl implements CashierService {

    private final OrderRepository orderRepository;
    private final CashierLogRepository cashierLogRepository;

    private static final List<OrderStatus> INACTIVE_STATUSES =
            List.of(OrderStatus.SELESAI, OrderStatus.EXPIRED, OrderStatus.FAILED);

    private static final Map<OrderStatus, List<OrderStatus>> ALLOWED_FROM = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED_FROM.put(OrderStatus.DIPROSES, List.of(OrderStatus.PAID, OrderStatus.DIPROSES));
        ALLOWED_FROM.put(OrderStatus.SIAP, List.of(OrderStatus.DIPROSES, OrderStatus.SIAP));
        ALLOWED_FROM.put(OrderStatus.SELESAI, List.of(OrderStatus.SIAP, OrderStatus.SELESAI));
    }

    @Override
    @Transactional(readOnly = true)
    public CashierOrderListResponse listActiveOrders() {
        List<Order> orders = orderRepository.findAllByStatusNotInOrderByCreatedAtDesc(INACTIVE_STATUSES);

        List<CashierOrderView> views = orders.stream().map(this::toCashierView).toList();
        return CashierOrderListResponse.builder().orders(views).build();
    }

    @Override
    public ConfirmPaymentResponse confirmPayment(String orderCode, String confirmedBy) {
        Order order = getOrderOrThrow(orderCode);

        if (order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.DIPROSES
                || order.getStatus() == OrderStatus.SIAP
                || order.getStatus() == OrderStatus.SELESAI) {
            throw new AlreadyPaidException();
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.PAID);
        if (order.getPayment() != null) {
            order.getPayment().setStatus(PaymentStatus.PAID);
            order.getPayment().setPaidAt(now);
            order.getPayment().setConfirmedBy(confirmedBy);
        }
        orderRepository.save(order);

        writeLog(order, confirmedBy, CashierAction.CONFIRM_PAYMENT, "Konfirmasi pembayaran cash");

        return ConfirmPaymentResponse.builder()
                .orderId(order.getOrderCode())
                .status(order.getStatus())
                .confirmedBy(confirmedBy)
                .paidAt(now)
                .build();
    }

    @Override
    public MarkSyncedResponse markSynced(String orderCode, String markedBy) {
        Order order = getOrderOrThrow(orderCode);

        LocalDateTime now = LocalDateTime.now();
        order.setSyncedManual(true);
        order.setSyncedMarkedBy(markedBy);
        order.setSyncedMarkedAt(now);
        orderRepository.save(order);

        writeLog(order, markedBy, CashierAction.MARK_SYNCED, "Ditandai sudah diinput ke Moka POS");

        return MarkSyncedResponse.builder()
                .orderId(order.getOrderCode())
                .syncedManual(true)
                .markedAt(now)
                .build();
    }

    @Override
    public UpdateStatusResponse updateStatus(String orderCode, String newStatusRaw) {
        Order order = getOrderOrThrow(orderCode);

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusRaw);
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusTransitionException(
                    "Status '" + newStatusRaw + "' tidak dikenali. Nilai valid: DIPROSES, SIAP, SELESAI");
        }

        List<OrderStatus> allowedFrom = ALLOWED_FROM.get(newStatus);
        if (allowedFrom == null || !allowedFrom.contains(order.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Order harus berstatus PAID sebelum bisa diproses");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        writeLog(order, "system", CashierAction.STATUS_CHANGE, "Status diubah menjadi " + newStatus);

        return UpdateStatusResponse.builder()
                .orderId(order.getOrderCode())
                .status(order.getStatus())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private Order getOrderOrThrow(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));
    }

    private void writeLog(Order order, String aktor, CashierAction aksi, String keterangan) {
        CashierLog log = CashierLog.builder()
                .order(order)
                .aktor(aktor)
                .aksi(aksi)
                .keterangan(keterangan)
                .build();
        cashierLogRepository.save(log);
    }

    private CashierOrderView toCashierView(Order order) {
        List<CashierOrderItemView> itemViews = order.getItems().stream()
                .map(this::toItemView)
                .toList();

        return CashierOrderView.builder()
                .orderId(order.getOrderCode())
                .tableNumber(order.getTable().getNomorMeja())
                .status(order.getStatus())
                .paymentMethod(order.getMetodePembayaran())
                .syncedManual(order.isSyncedManual())
                .items(itemViews)
                .totalAmount(order.getTotalHarga())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private CashierOrderItemView toItemView(OrderItem item) {
        return CashierOrderItemView.builder()
                .name(item.getNamaItemSnapshot())
                .qty(item.getQty())
                .note(item.getCatatan())
                .build();
    }
}
