package com.tableorder.dto.response;

import com.tableorder.entity.enums.OrderStatus;
import com.tableorder.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {
    private String orderId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
}
