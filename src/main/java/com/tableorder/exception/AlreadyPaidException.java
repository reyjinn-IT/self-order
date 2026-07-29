package com.tableorder.exception;

import org.springframework.http.HttpStatus;

public class AlreadyPaidException extends ApiException {
    public AlreadyPaidException() {
        super(HttpStatus.CONFLICT, "ALREADY_PAID", "Order sudah berstatus PAID");
    }
}
