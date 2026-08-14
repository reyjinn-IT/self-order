package com.tableorder.service.impl;

import com.tableorder.dto.request.MenuCategoryRequest;
import com.tableorder.dto.request.MenuItemRequest;
import com.tableorder.entity.MenuCategory;
import com.tableorder.entity.MenuItem;
import com.tableorder.exception.ApiException;
import com.tableorder.exception.ItemUnavailableException;
import com.tableorder.repository.MenuCategoryRepository;
import com.tableorder.repository.MenuItemRepository;
import com.tableorder.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuServiceImpl implements MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategory> listCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public MenuCategory createCategory(MenuCategoryRequest request) {
        MenuCategory category = MenuCategory.builder()
                .namaKategori(request.getNamaKategori())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItem> listAvailableItems() {
        return itemRepository.findAllByStatusTersediaTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItem> listAllItemsForAdmin() {
        return itemRepository.findAllWithCategory();
    }

    @Override
    public MenuItem createItem(MenuItemRequest request) {
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND",
                        "Kategori dengan id " + request.getCategoryId() + " tidak ditemukan"));

        MenuItem item = MenuItem.builder()
                .itemCode(request.getItemCode())
                .category(category)
                .nama(request.getNama())
                .harga(request.getHarga())
                .deskripsi(request.getDeskripsi())
                .fotoUrl(request.getFotoUrl())
                .statusTersedia(request.getStatusTersedia() == null || request.getStatusTersedia())
                .build();
        return itemRepository.save(item);
    }

    @Override
    public MenuItem updateItem(Long itemId, MenuItemRequest request) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND",
                        "Item dengan id " + itemId + " tidak ditemukan"));

        if (request.getCategoryId() != null) {
            MenuCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND",
                            "Kategori dengan id " + request.getCategoryId() + " tidak ditemukan"));
            item.setCategory(category);
        }
        if (request.getNama() != null) item.setNama(request.getNama());
        if (request.getHarga() != null) item.setHarga(request.getHarga());
        if (request.getDeskripsi() != null) item.setDeskripsi(request.getDeskripsi());
        if (request.getFotoUrl() != null) item.setFotoUrl(request.getFotoUrl());
        if (request.getStatusTersedia() != null) item.setStatusTersedia(request.getStatusTersedia());

        return itemRepository.save(item);
    }

    @Override
    public MenuItem setItemAvailability(Long itemId, boolean available) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND",
                        "Item dengan id " + itemId + " tidak ditemukan"));
        item.setStatusTersedia(available);
        return itemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItem findByItemCode(String itemCode) {
        MenuItem item = itemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND",
                        "Item " + itemCode + " tidak ditemukan"));
        if (!item.isStatusTersedia()) {
            throw new ItemUnavailableException(itemCode);
        }
        return item;
    }
}
