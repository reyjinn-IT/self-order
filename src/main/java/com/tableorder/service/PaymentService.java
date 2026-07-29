package com.tableorder.service;

import com.tableorder.dto.request.MidtransNotificationRequest;
import com.tableorder.dto.response.PaymentSnapInfo;
import com.tableorder.entity.Order;

public interface PaymentService {

    /** Buat transaksi Snap ke Midtrans untuk order QRIS yang baru dibuat. */
    PaymentSnapInfo createSnapTransaction(Order order);

    /** Proses webhook notifikasi dari Midtrans. Melempar InvalidSignatureException kalau signature salah. */
    void handleNotification(MidtransNotificationRequest notification);

    /** Fallback: cek status transaksi langsung ke Midtrans API (FR-10), dipakai jika webhook gagal diterima. */
    void checkStatusToMidtrans(String orderCode);
}
