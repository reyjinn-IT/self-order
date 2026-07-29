package com.tableorder.controller;

import com.tableorder.dto.request.TableRequest;
import com.tableorder.entity.DiningTable;
import com.tableorder.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FR-19: Admin dapat mengelola daftar meja & generate QR token per meja.
 */
@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class AdminTableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<DiningTable>> listTables() {
        return ResponseEntity.ok(tableService.listAll());
    }

    @PostMapping
    public ResponseEntity<DiningTable> createTable(@Valid @RequestBody TableRequest request) {
        String qrToken = (request.getQrToken() != null && !request.getQrToken().isBlank())
                ? request.getQrToken()
                : "TBL-QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tableService.create(request.getNomorMeja(), qrToken));
    }

    @PatchMapping("/{tableId}/active")
    public ResponseEntity<DiningTable> setActive(@PathVariable Long tableId,
                                                   @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("is_active"));
        return ResponseEntity.ok(tableService.setActive(tableId, active));
    }
}
