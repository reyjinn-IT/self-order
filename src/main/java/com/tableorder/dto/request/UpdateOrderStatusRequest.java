package com.tableorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotBlank(message = "status wajib diisi")
    private String status; // DIPROSES | SIAP | SELESAI
}
