package com.tableorder.entity.enums;

/**
 * State machine sesuai PRD MVP Bagian 5:
 *
 * PENDING_PAYMENT -> PAID -> DIPROSES -> SIAP -> SELESAI
 * PENDING_PAYMENT -> EXPIRED
 * PENDING_PAYMENT -> FAILED
 *
 * synced_manual adalah flag terpisah (bukan bagian status utama).
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    EXPIRED,
    FAILED,
    DIPROSES,
    SIAP,
    SELESAI
}
