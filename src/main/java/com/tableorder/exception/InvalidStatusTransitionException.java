package com.tableorder.exception;

import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends ApiException {
    public InvalidStatusTransitionException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATUS_TRANSITION", message);
    }
}
