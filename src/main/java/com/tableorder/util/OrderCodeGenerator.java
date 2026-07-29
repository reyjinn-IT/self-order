package com.tableorder.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generate order_id publik dengan format ORD-YYYYMMDD-NNNNNN
 * sesuai contoh di API Contract MVP (mis. ORD-20260729-000045).
 *
 * Catatan implementasi: counter in-memory ini cukup untuk MVP single-instance.
 * Untuk multi-instance/high-concurrency, ganti dengan sequence DB (mis. tabel
 * counter per-hari dengan SELECT ... FOR UPDATE) agar tidak terjadi tabrakan.
 */
@Component
public class OrderCodeGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AtomicLong counter = new AtomicLong(0);
    private volatile String currentDateKey = "";

    public synchronized String generate() {
        String todayKey = LocalDate.now().format(DATE_FMT);
        if (!todayKey.equals(currentDateKey)) {
            currentDateKey = todayKey;
            counter.set(0);
        }
        long seq = counter.incrementAndGet();
        return String.format("ORD-%s-%06d", currentDateKey, seq);
    }
}
