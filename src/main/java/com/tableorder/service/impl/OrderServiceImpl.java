package com.tableorder.service.impl;

import com.tableorder.dto.request.CreateOrderRequest;
import com.tableorder.dto.response.CreateOrderResponse;
import com.tableorder.dto.response.OrderStatusResponse;
import com.tableorder.dto.response.PaymentSnapInfo;
import com.tableorder.entity.DiningTable;
import com.tableorder.entity.MenuItem;
import com.tableorder.entity.Order;
import com.tableorder.entity.OrderItem;
import com.tableorder.entity.Payment;
import com.tableorder.entity.enums.OrderStatus;
import com.tableorder.entity.enums.PaymentMethod;
import com.tableorder.entity.enums.PaymentStatus;
import com.tableorder.exception.OrderNotFoundException;
import com.tableorder.repository.OrderRepository;
import com.tableorder.service.MenuService;
import com.tableorder.service.OrderService;
import com.tableorder.service.PaymentService;
import com.tableorder.service.TableService;
import com.tableorder.util.OrderCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final TableService tableService;
    private final MenuService menuService;
    private final PaymentService paymentService;
    private final OrderCodeGenerator orderCodeGenerator;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        DiningTable table = tableService.resolveByQrToken(request.getTableToken());

        Order order = Order.builder()
                .orderCode(orderCodeGenerator.generate())
                .table(table)
                .status(OrderStatus.PENDING_PAYMENT)
                .metodePembayaran(request.getPaymentMethod())
                .totalHarga(BigDecimal.ZERO)
                .syncedManual(false)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            // findByItemCode juga memvalidasi status_tersedia -> lempar ITEM_UNAVAILABLE kalau tidak aktif
            MenuItem menuItem = menuService.findByItemCode(itemReq.getItemId());

            BigDecimal subtotal = menuItem.getHarga().multiply(BigDecimal.valueOf(itemReq.getQty()));
            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .namaItemSnapshot(menuItem.getNama())
                    .hargaSnapshot(menuItem.getHarga())
                    .qty(itemReq.getQty())
                    .catatan(itemReq.getNote())
                    .subtotal(subtotal)
                    .build();
            order.addItem(orderItem);
            total = total.add(subtotal);
        }
        order.setTotalHarga(total);

        Payment payment = Payment.builder()
                .order(order)
                .metode(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .grossAmount(total)
                .build();
        order.setPayment(payment);

        Order saved = orderRepository.save(order);

        PaymentSnapInfo snapInfo = null;
        if (request.getPaymentMethod() == PaymentMethod.QRIS) {
            snapInfo = paymentService.createSnapTransaction(saved);
        }

        return CreateOrderResponse.builder()
                .orderId(saved.getOrderCode())
                .tableNumber(table.getNomorMeja())
                .status(saved.getStatus())
                .paymentMethod(saved.getMetodePembayaran())
                .totalAmount(saved.getTotalHarga())
                .createdAt(saved.getCreatedAt())
                .payment(snapInfo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));

        PaymentStatus paymentStatus = order.getPayment() != null
                ? order.getPayment().getStatus()
                : PaymentStatus.PENDING;

        return OrderStatusResponse.builder()
                .orderId(order.getOrderCode())
                .status(order.getStatus())
                .paymentStatus(paymentStatus)
                .build();
    }
}
