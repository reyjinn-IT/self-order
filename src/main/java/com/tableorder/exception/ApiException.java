package com.tableorder.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception untuk error bisnis yang harus dipetakan ke response
 * berformat { "error_code": ..., "message": ... } sesuai API Contract MVP.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public ApiException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
