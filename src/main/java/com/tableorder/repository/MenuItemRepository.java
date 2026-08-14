package com.tableorder.repository;

import com.tableorder.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByItemCode(String itemCode);

    @Query("SELECT m FROM MenuItem m JOIN FETCH m.category WHERE m.statusTersedia = true")
    List<MenuItem> findAllByStatusTersediaTrue();

    @Query("SELECT m FROM MenuItem m JOIN FETCH m.category")
    List<MenuItem> findAllWithCategory();
}
