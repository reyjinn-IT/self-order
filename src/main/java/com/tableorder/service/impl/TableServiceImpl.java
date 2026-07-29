package com.tableorder.service.impl;

import com.tableorder.entity.DiningTable;
import com.tableorder.exception.TableNotFoundException;
import com.tableorder.repository.DiningTableRepository;
import com.tableorder.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TableServiceImpl implements TableService {

    private final DiningTableRepository tableRepository;

    @Override
    @Transactional(readOnly = true)
    public DiningTable resolveByQrToken(String qrToken) {
        return tableRepository.findByQrTokenAndActiveTrue(qrToken)
                .orElseThrow(() -> new TableNotFoundException(qrToken));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiningTable> listAll() {
        return tableRepository.findAll();
    }

    @Override
    public DiningTable create(String nomorMeja, String qrToken) {
        DiningTable table = DiningTable.builder()
                .nomorMeja(nomorMeja)
                .qrToken(qrToken)
                .active(true)
                .build();
        return tableRepository.save(table);
    }

    @Override
    public DiningTable setActive(Long tableId, boolean active) {
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException("id:" + tableId));
        table.setActive(active);
        return tableRepository.save(table);
    }
}
