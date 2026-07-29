package com.tableorder.entity;

import com.tableorder.entity.enums.CashierAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cashier_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashierLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "aktor", nullable = false, length = 100)
    private String aktor;

    @Enumerated(EnumType.STRING)
    @Column(name = "aksi", nullable = false, length = 50)
    private CashierAction aksi;

    @Column(name = "keterangan", length = 500)
    private String keterangan;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
