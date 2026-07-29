package com.tableorder.repository;

import com.tableorder.entity.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    Optional<DiningTable> findByQrTokenAndActiveTrue(String qrToken);
}
