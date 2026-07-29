package com.tableorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemRequest {
    @NotBlank(message = "item_code wajib diisi")
    private String itemCode;

    @NotNull(message = "category_id wajib diisi")
    private Long categoryId;

    @NotBlank(message = "nama wajib diisi")
    private String nama;

    @NotNull(message = "harga wajib diisi")
    @Positive(message = "harga harus lebih dari 0")
    private BigDecimal harga;

    private String deskripsi;
    private String fotoUrl;
    private Boolean statusTersedia;
}
