package com.tableorder.dto.response;

import com.tableorder.entity.enums.OrderStatus;
import com.tableorder.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashierOrderView {
    private String orderId;
    private String tableNumber;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private boolean syncedManual;
    private List<CashierOrderItemView> items;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
