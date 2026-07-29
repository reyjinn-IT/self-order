package com.tableorder.service;

import com.tableorder.dto.request.MenuCategoryRequest;
import com.tableorder.dto.request.MenuItemRequest;
import com.tableorder.entity.MenuCategory;
import com.tableorder.entity.MenuItem;

import java.util.List;

public interface MenuService {
    List<MenuCategory> listCategories();
    MenuCategory createCategory(MenuCategoryRequest request);

    List<MenuItem> listAvailableItems();
    List<MenuItem> listAllItemsForAdmin();
    MenuItem createItem(MenuItemRequest request);
    MenuItem updateItem(Long itemId, MenuItemRequest request);
    MenuItem setItemAvailability(Long itemId, boolean available);
    MenuItem findByItemCode(String itemCode);
}
