package com.tableorder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSnapInfo {
    private String snapToken;
    private String snapRedirectUrl;
    private LocalDateTime expiryTime;
}
