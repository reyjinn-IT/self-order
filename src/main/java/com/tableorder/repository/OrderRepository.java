package com.tableorder.repository;

import com.tableorder.entity.Order;
import com.tableorder.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findAllByStatusNotInOrderByCreatedAtDesc(List<OrderStatus> excludedStatuses);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findAllByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime before);
}
