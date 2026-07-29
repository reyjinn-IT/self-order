package com.tableorder.repository;

import com.tableorder.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_OrderCode(String orderCode);
    Optional<Payment> findByMidtransTransactionId(String midtransTransactionId);
}
