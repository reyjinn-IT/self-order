package com.tableorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmPaymentRequest {
    @NotBlank(message = "confirmed_by wajib diisi")
    private String confirmedBy;
}
