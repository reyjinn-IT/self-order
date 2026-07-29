package com.tableorder.exception;

import org.springframework.http.HttpStatus;

public class TableNotFoundException extends ApiException {
    public TableNotFoundException(String tableToken) {
        super(HttpStatus.NOT_FOUND, "TABLE_NOT_FOUND",
                "Meja dengan token " + tableToken + " tidak ditemukan atau tidak aktif");
    }
}
