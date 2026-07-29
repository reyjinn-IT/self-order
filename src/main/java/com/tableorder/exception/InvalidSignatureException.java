package com.tableorder.exception;

import org.springframework.http.HttpStatus;

public class InvalidSignatureException extends ApiException {
    public InvalidSignatureException() {
        super(HttpStatus.FORBIDDEN, "invalid_signature", "Signature key tidak valid");
    }
}
