package com.tableorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuCategoryRequest {
    @NotBlank(message = "nama_kategori wajib diisi")
    private String namaKategori;

    private Integer sortOrder;
}
