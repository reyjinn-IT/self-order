package com.tableorder.dto.request;

import lombok.Data;

/**
 * Payload webhook dari Midtrans (POST /api/payments/midtrans/notification).
 * Field mengikuti nama asli dari Midtrans (snake_case, sudah cocok dengan
 * Jackson SNAKE_CASE naming strategy).
 */
@Data
public class MidtransNotificationRequest {
    private String transactionTime;
    private String transactionStatus;
    private String transactionId;
    private String statusCode;
    private String signatureKey;
    private String paymentType;
    private String orderId;
    private String grossAmount;
    private String fraudStatus;
}
