package com.tableorder.dto.request;

import com.tableorder.entity.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "table_token wajib diisi")
    private String tableToken;

    @NotEmpty(message = "items tidak boleh kosong")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "payment_method wajib diisi")
    private PaymentMethod paymentMethod;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "item_id wajib diisi")
        private String itemId;

        @NotNull(message = "qty wajib diisi")
        private Integer qty;

        private String note;
    }
}
