package com.tableorder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    /** Snapshot nama & harga saat order dibuat, supaya histori tidak berubah jika menu diedit */
    @Column(name = "nama_item_snapshot", nullable = false, length = 150)
    private String namaItemSnapshot;

    @Column(name = "harga_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal hargaSnapshot;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Column(name = "catatan", length = 300)
    private String catatan;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
