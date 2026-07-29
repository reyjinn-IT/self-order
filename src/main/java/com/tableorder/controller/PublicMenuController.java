package com.tableorder.controller;

import com.tableorder.entity.MenuCategory;
import com.tableorder.entity.MenuItem;
import com.tableorder.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * FR-02: Pelanggan melihat menu per kategori (nama, harga, deskripsi, foto, status tersedia).
 * Dipakai oleh Customer Ordering App setelah scan QR.
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class PublicMenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<List<MenuCategory>> listCategories() {
        return ResponseEntity.ok(menuService.listCategories());
    }

    @GetMapping("/items")
    public ResponseEntity<List<MenuItem>> listAvailableItems() {
        return ResponseEntity.ok(menuService.listAvailableItems());
    }
}
