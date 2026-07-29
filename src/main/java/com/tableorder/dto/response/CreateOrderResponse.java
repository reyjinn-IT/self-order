package com.tableorder.dto.response;

import com.tableorder.entity.enums.OrderStatus;
import com.tableorder.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private String orderId;
    private String tableNumber;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private PaymentSnapInfo payment; // null kalau CASH_COUNTER
}
