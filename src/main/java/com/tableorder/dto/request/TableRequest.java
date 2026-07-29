package com.tableorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TableRequest {
    @NotBlank(message = "nomor_meja wajib diisi")
    private String nomorMeja;

    private String qrToken; // opsional, auto-generate kalau kosong
}
