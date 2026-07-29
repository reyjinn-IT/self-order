package com.tableorder.repository;

import com.tableorder.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByItemCode(String itemCode);
    List<MenuItem> findAllByStatusTersediaTrue();
}
