package com.tableorder.controller;

import com.tableorder.dto.request.MenuCategoryRequest;
import com.tableorder.dto.request.MenuItemRequest;
import com.tableorder.entity.MenuCategory;
import com.tableorder.entity.MenuItem;
import com.tableorder.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FR-18: Admin dapat tambah/edit/nonaktifkan item menu.
 * Bukan bagian dari API Contract MVP eksplisit, tapi wajib ada sesuai
 * BR-08 / FR-18 di PRD MVP. Path diawali /api/admin sesuai konvensi role admin.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    // ---- Kategori ----
    @GetMapping("/menu-categories")
    public ResponseEntity<List<MenuCategory>> listCategories() {
        return ResponseEntity.ok(menuService.listCategories());
    }

    @PostMapping("/menu-categories")
    public ResponseEntity<MenuCategory> createCategory(@Valid @RequestBody MenuCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createCategory(request));
    }

    // ---- Item Menu ----
    @GetMapping("/menu-items")
    public ResponseEntity<List<MenuItem>> listItems() {
        return ResponseEntity.ok(menuService.listAllItemsForAdmin());
    }

    @PostMapping("/menu-items")
    public ResponseEntity<MenuItem> createItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createItem(request));
    }

    @PutMapping("/menu-items/{itemId}")
    public ResponseEntity<MenuItem> updateItem(@PathVariable Long itemId,
                                                @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateItem(itemId, request));
    }

    @PatchMapping("/menu-items/{itemId}/availability")
    public ResponseEntity<MenuItem> setAvailability(@PathVariable Long itemId,
                                                      @RequestBody Map<String, Boolean> body) {
        boolean available = Boolean.TRUE.equals(body.get("status_tersedia"));
        return ResponseEntity.ok(menuService.setItemAvailability(itemId, available));
    }
}
