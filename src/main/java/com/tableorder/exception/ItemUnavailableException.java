package com.tableorder.exception;

import org.springframework.http.HttpStatus;

public class ItemUnavailableException extends ApiException {
    public ItemUnavailableException(String itemCode) {
        super(HttpStatus.BAD_REQUEST, "ITEM_UNAVAILABLE",
                "Item " + itemCode + " sedang tidak tersedia");
    }
}
