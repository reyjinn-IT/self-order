# API Contract — Contoh Request & Response (MVP)
## Sistem Pemesanan Makanan dari Meja

**Versi:** 1.0
**Referensi:** PRD MVP v2.0

Catatan: semua endpoint & path di bawah ini bersifat **usulan/ilustratif** untuk acuan development — nama path final menyesuaikan konvensi backend yang dipakai.

---

## 1. Customer — Buat Order (Checkout)

`POST /api/orders`

**Request:**
```json
{
  "table_token": "TBL-QR-000012",
  "items": [
    { "item_id": "MENU-045", "qty": 2, "note": "pedas sedang" },
    { "item_id": "MENU-102", "qty": 2, "note": "less ice" }
  ],
  "payment_method": "QRIS"
}
```

**Response `201 Created`** (metode `QRIS`):
```json
{
  "order_id": "ORD-20260729-000045",
  "table_number": "12",
  "status": "PENDING_PAYMENT",
  "payment_method": "QRIS",
  "total_amount": 72000,
  "created_at": "2026-07-29T10:15:00+07:00",
  "payment": {
    "snap_token": "66e4fa55-3a21-4f2e-9c10-abcdef123456",
    "snap_redirect_url": "https://app.sandbox.midtrans.com/snap/v4/redirection/66e4fa55-...",
    "expiry_time": "2026-07-29T10:30:00+07:00"
  }
}
```

**Response `201 Created`** (metode `CASH_COUNTER` — tanpa objek `payment`):
```json
{
  "order_id": "ORD-20260729-000046",
  "table_number": "12",
  "status": "PENDING_PAYMENT",
  "payment_method": "CASH_COUNTER",
  "total_amount": 72000,
  "created_at": "2026-07-29T10:16:00+07:00"
}
```

**Response `400 Bad Request`** (contoh: item tidak tersedia):
```json
{
  "error_code": "ITEM_UNAVAILABLE",
  "message": "Item MENU-045 sedang tidak tersedia"
}
```

---

## 2. Midtrans — Webhook Notification Handler

`POST /api/payments/midtrans/notification`

**Request** (dikirim oleh Midtrans ke server kita):
```json
{
  "transaction_time": "2026-07-29 10:16:23",
  "transaction_status": "settlement",
  "transaction_id": "a1b2c3d4-5678-90ab-cdef-1234567890ab",
  "status_code": "200",
  "signature_key": "9b1e2f7c4a8d5e6f1a2b3c4d5e6f7a8b9c0d1e2f",
  "payment_type": "qris",
  "order_id": "ORD-20260729-000045",
  "gross_amount": "72000.00",
  "fraud_status": "accept"
}
```

**Response `200 OK`** (wajib dibalas ke Midtrans agar tidak retry terus):
```json
{
  "status": "ok"
}
```

**Response `403 Forbidden`** (kalau `signature_key` gagal validasi):
```json
{
  "status": "invalid_signature"
}
```

---

## 3. Companion Screen — Daftar Order Masuk

`GET /api/cashier/orders?status=active`

**Response `200 OK`:**
```json
{
  "orders": [
    {
      "order_id": "ORD-20260729-000045",
      "table_number": "12",
      "status": "PAID",
      "payment_method": "QRIS",
      "synced_manual": false,
      "items": [
        { "name": "Nasi Goreng Spesial", "qty": 2, "note": "pedas sedang" },
        { "name": "Es Teh Manis", "qty": 2, "note": "less ice" }
      ],
      "total_amount": 72000,
      "created_at": "2026-07-29T10:15:00+07:00"
    },
    {
      "order_id": "ORD-20260729-000046",
      "table_number": "07",
      "status": "PENDING_PAYMENT",
      "payment_method": "CASH_COUNTER",
      "synced_manual": false,
      "items": [
        { "name": "Ayam Bakar", "qty": 1, "note": "" }
      ],
      "total_amount": 35000,
      "created_at": "2026-07-29T10:16:00+07:00"
    }
  ]
}
```

---

## 4. Kasir — Konfirmasi Pembayaran Cash

`POST /api/cashier/orders/{order_id}/confirm-payment`

**Request:**
```json
{
  "confirmed_by": "kasir_01"
}
```

**Response `200 OK`:**
```json
{
  "order_id": "ORD-20260729-000046",
  "status": "PAID",
  "confirmed_by": "kasir_01",
  "paid_at": "2026-07-29T10:20:00+07:00"
}
```

**Response `409 Conflict`** (order sudah lunas sebelumnya):
```json
{
  "error_code": "ALREADY_PAID",
  "message": "Order sudah berstatus PAID"
}
```

---

## 5. Kasir — Tandai Order Sudah Diinput ke Moka POS

`POST /api/cashier/orders/{order_id}/mark-synced`

**Request:**
```json
{
  "marked_by": "kasir_01"
}
```

**Response `200 OK`:**
```json
{
  "order_id": "ORD-20260729-000045",
  "synced_manual": true,
  "marked_at": "2026-07-29T10:21:00+07:00"
}
```

---

## 6. Kasir — Update Status Order

`PATCH /api/cashier/orders/{order_id}/status`

**Request:**
```json
{
  "status": "DIPROSES"
}
```
*(nilai valid: `DIPROSES`, `SIAP`, `SELESAI`)*

**Response `200 OK`:**
```json
{
  "order_id": "ORD-20260729-000045",
  "status": "DIPROSES",
  "updated_at": "2026-07-29T10:22:00+07:00"
}
```

**Response `422 Unprocessable Entity`** (contoh: order belum `PAID` tapi dipaksa `DIPROSES`):
```json
{
  "error_code": "INVALID_STATUS_TRANSITION",
  "message": "Order harus berstatus PAID sebelum bisa diproses"
}
```

---

## 7. Customer — Cek Status Order (polling di halaman order pelanggan)

`GET /api/orders/{order_id}/status`

**Response `200 OK`:**
```json
{
  "order_id": "ORD-20260729-000045",
  "status": "DIPROSES",
  "payment_status": "PAID"
}
```
