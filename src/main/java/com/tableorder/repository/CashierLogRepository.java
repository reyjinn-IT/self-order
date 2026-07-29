package com.tableorder.repository;

import com.tableorder.entity.CashierLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashierLogRepository extends JpaRepository<CashierLog, Long> {
    List<CashierLog> findAllByOrder_OrderIdOrderByTimestampAsc(Long orderId);
}
