package com.tableorder.dto.response;

import com.tableorder.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusResponse {
    private String orderId;
    private OrderStatus status;
    private LocalDateTime updatedAt;
}
