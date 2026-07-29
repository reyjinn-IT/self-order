package com.tableorder.service;

import com.tableorder.entity.DiningTable;

import java.util.List;

public interface TableService {
    DiningTable resolveByQrToken(String qrToken);
    List<DiningTable> listAll();
    DiningTable create(String nomorMeja, String qrToken);
    DiningTable setActive(Long tableId, boolean active);
}
