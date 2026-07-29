package com.tableorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarkSyncedRequest {
    @NotBlank(message = "marked_by wajib diisi")
    private String markedBy;
}
