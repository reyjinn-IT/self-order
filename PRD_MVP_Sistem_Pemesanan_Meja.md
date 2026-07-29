# Product Requirements Document (PRD) — MVP
## Sistem Pemesanan Makanan dari Meja

**Versi:** 2.0 (MVP)
**Tanggal:** 29 Juli 2026
**Status:** Draft
**Referensi:** PRD v1.0 (full scope), BRD MVP v2.0

---

## 1. Overview

MVP terdiri dari 3 komponen:

1. **Customer Ordering App** — web app, scan QR meja → menu → cart → checkout → bayar.
2. **Payment Module** — QRIS via Midtrans (Snap API) + Bayar di kasir (manual).
3. **Companion Screen (Kasir)** — layar terpisah yang menampilkan order masuk real-time; kasir input manual ke Moka POS existing dan menandai order sebagai "sudah diproses" di sistem ini.

**Tidak ada integrasi direct API ke Moka di MVP** — ini yang membedakan dari PRD v1.0. Semua desain data/API dibuat agar mudah "disambung" ke Advanced Ordering API Moka nanti tanpa perombakan besar (lihat Bagian 8).

### 1.1 User Roles MVP
| Role | Akses |
|------|-------|
| Customer | Scan QR, browse menu, order, checkout, bayar |
| Kasir | Companion Screen: lihat order masuk, konfirmasi cash, tandai status |
| Admin | Kelola menu (CRUD dasar), kelola meja/QR |

---

## 2. User Flow MVP

### 2.1 Flow Customer
1. Scan QR di meja → `table_id` terdeteksi otomatis.
2. Browse menu per kategori → tambah ke cart (qty + catatan).
3. Checkout → pilih metode bayar: **QRIS (Midtrans)** atau **Bayar di Kasir**.
4. Sistem generate `order_id`, status awal `PENDING_PAYMENT`.

### 2.2 Flow Bayar QRIS (Midtrans Snap)
1. Backend request Snap transaction ke Midtrans dengan `order_id` sebagai reference.
2. Pelanggan diarahkan ke Snap popup/redirect, scan QRIS, bayar.
3. Midtrans kirim webhook notification → backend update status `PAID` (atau `EXPIRED`/`FAILED`).
4. Order berstatus `PAID` otomatis muncul di Companion Screen.

### 2.3 Flow Bayar di Kasir
1. Order dibuat status `PENDING_PAYMENT`, metode `CASH_COUNTER`, langsung tampil di Companion Screen berlabel "Menunggu Pembayaran".
2. Pelanggan datang ke kasir, sebut `order_id`/nomor meja.
3. Kasir cari order di Companion Screen → klik "Konfirmasi Pembayaran" → status jadi `PAID`.

### 2.4 Flow Kasir — Companion Screen
1. Order baru (status apa pun) muncul real-time (polling interval pendek atau websocket sederhana).
2. Kasir lihat detail: meja, item, catatan, metode & status bayar.
3. Setelah `PAID`, kasir **input manual ke Moka POS** seperti biasa.
4. Kasir klik **"Tandai Sudah Diinput ke POS"** di Companion Screen → order dapat status internal `SYNCED_MANUAL` supaya gampang dibedakan dari yang belum diproses (highlight visual untuk yang belum ditandai).
5. Kasir update status order (Diproses → Siap → Selesai) di Companion Screen untuk keperluan tracking dapur/pengantaran.

---

## 3. Functional Requirements MVP

### 3.1 Customer Ordering
| ID | Requirement |
|----|-------------|
| FR-01 | Sistem mengenali nomor meja dari QR code (unique table token) |
| FR-02 | Pelanggan melihat menu per kategori (nama, harga, deskripsi, foto, status tersedia) |
| FR-03 | Pelanggan dapat menambah/kurangi item di cart + catatan per item |
| FR-04 | Pelanggan melihat ringkasan & total sebelum checkout |
| FR-05 | Pelanggan memilih metode pembayaran saat checkout |
| FR-06 | Pelanggan melihat status order-nya (menunggu bayar/dibayar/diproses/siap) |

### 3.2 Payment — Midtrans QRIS
| ID | Requirement |
|----|-------------|
| FR-07 | Sistem membuat transaksi Midtrans Snap dengan `order_id` sebagai reference |
| FR-08 | Sistem punya webhook handler untuk notifikasi status dari Midtrans, dengan validasi signature |
| FR-09 | Order otomatis `EXPIRED` jika tidak dibayar dalam batas waktu Midtrans |
| FR-10 | Tersedia status-check API sebagai fallback jika webhook gagal diterima |

### 3.3 Payment — Bayar di Kasir
| ID | Requirement |
|----|-------------|
| FR-11 | Order metode `CASH_COUNTER` dibuat tanpa proses Midtrans |
| FR-12 | Kasir dapat mencari order (by `order_id`/nomor meja) dan konfirmasi pembayaran manual, tercatat aktor & waktu |

### 3.4 Companion Screen (Kasir)
| ID | Requirement |
|----|-------------|
| FR-13 | Daftar order masuk tampil real-time, terurut terbaru |
| FR-14 | Order yang belum ditandai "sudah diinput ke POS" ditampilkan menonjol (highlight) |
| FR-15 | Kasir dapat menandai order "sudah diinput ke POS" (mencegah dobel input) |
| FR-16 | Kasir dapat update status order: Diproses / Siap / Selesai |
| FR-17 | Setiap perubahan status tercatat log (aktor + waktu) |

### 3.5 Admin — Manajemen Menu
| ID | Requirement |
|----|-------------|
| FR-18 | Admin dapat tambah/edit/nonaktifkan item menu (nama, harga, kategori, foto, status tersedia) |
| FR-19 | Admin dapat mengelola daftar meja & generate QR token per meja |

---

## 4. Non-Functional Requirements MVP

| Kategori | Requirement |
|----------|-------------|
| Performance | Order tampil di Companion Screen maksimal 3-5 detik setelah checkout |
| Reliability | `CASH_COUNTER` tetap berfungsi penuh meski Midtrans gangguan |
| Security | Webhook Midtrans wajib validasi signature; tidak menyimpan data kartu/pembayaran sensitif |
| Usability | Customer app mobile-first; Companion Screen dioptimalkan untuk tablet/monitor kasir |

---

## 5. Order Status — State Machine MVP

```
PENDING_PAYMENT
    ├── (QRIS lunas via Midtrans) ──► PAID
    ├── (expired) ──► EXPIRED
    ├── (kasir konfirmasi cash) ──► PAID

PAID ──► [ditandai kasir: SYNCED_MANUAL] ──► DIPROSES ──► SIAP ──► SELESAI
```

*(`SYNCED_MANUAL` adalah flag tambahan, bukan pengganti status utama — dipakai khusus untuk mencegah double-entry ke Moka POS selama Opsi A belum tersedia.)*

---

## 6. Data Model MVP

| Entitas | Atribut Kunci |
|---------|----------------|
| **Table** | table_id, nomor_meja, qr_token |
| **MenuCategory** | category_id, nama_kategori |
| **MenuItem** | item_id, category_id, nama, harga, deskripsi, foto, status_tersedia |
| **Order** | order_id, table_id, status, metode_pembayaran, total_harga, synced_manual (bool), created_at |
| **OrderItem** | order_item_id, order_id, item_id, qty, catatan, subtotal |
| **Payment** | payment_id, order_id, metode, midtrans_transaction_id (nullable), status, paid_at, confirmed_by |
| **CashierLog** | log_id, order_id, aktor, aksi, timestamp |

*(Struktur ini sengaja dibuat identik dengan versi full — supaya saat Opsi A/Advanced Ordering API Moka tersedia, cukup tambah field `external_pos_order_ref` dan proses push otomatis, tanpa redesain data.)*

---

## 7. Integrasi Midtrans (ringkas, tidak berubah dari v1.0)

- Pakai **Snap API** untuk kecepatan implementasi (UI pembayaran siap pakai).
- Webhook wajib validasi `signature_key`.
- Mapping status: `pending`→`PENDING_PAYMENT`, `settlement`/`capture`(accept)→`PAID`, `expire`→`EXPIRED`, `deny`/`cancel`→`FAILED`.

---

## 8. Jalur Upgrade ke Opsi A (Integrasi Direct Moka)

MVP dirancang supaya migrasi ke direct API tidak perlu bongkar ulang:

1. Setelah akses **Advanced Ordering API** Moka didapat, tambahkan service baru yang men-subscribe event `PAID` dan push ke endpoint resmi Moka (mapping `item_id` ke ID item Moka backoffice).
2. Field `synced_manual` di Companion Screen bisa dinonaktifkan/disembunyikan begitu push otomatis berjalan stabil (tidak perlu tandai manual lagi).
3. Companion Screen tetap dipertahankan sebagai fallback/monitoring, bukan dihapus — berguna saat API Moka down.

---

## 9. Acceptance Criteria MVP (User Stories)

- **US-01:** Sebagai pelanggan, saya scan QR di meja dan langsung lihat menu tanpa login, sehingga saya bisa segera memesan.
- **US-02:** Sebagai pelanggan, saya bisa bayar QRIS langsung dari HP saya tanpa ke kasir.
- **US-03:** Sebagai pelanggan, saya bisa pilih bayar di kasir kalau tidak pakai e-wallet.
- **US-04:** Sebagai kasir, saya lihat order baru real-time di Companion Screen tanpa perlu didatangi pelanggan/pelayan.
- **US-05:** Sebagai kasir, saya bisa konfirmasi pembayaran cash manual dari Companion Screen.
- **US-06:** Sebagai kasir, saya bisa menandai order yang sudah saya input ke Moka POS, supaya saya tidak input dobel.
- **US-07:** Sebagai admin, saya bisa menambah/mengedit item menu tanpa bantuan developer.

---

## 10. Out of Scope MVP

- Integrasi direct API ke Moka POS (Opsi A) — roadmap fase berikutnya.
- E-wallet selain QRIS.
- Cetak struk otomatis.
- Laporan/analytics & rekonsiliasi otomatis.
- Kitchen Display System.
- Fitur panggil pelayan, notifikasi WA/SMS.
- Multi-outlet.

---

## 11. Open Questions MVP

1. Target berapa lama masa uji coba MVP sebelum evaluasi lanjut ke fase berikutnya?
2. Companion Screen pakai device apa (tablet dedicated / monitor + browser)?
3. Apakah butuh role terpisah untuk admin vs kasir (login berbeda), atau cukup satu akses shared untuk MVP?

---

*Dokumen ini adalah versi MVP yang menggantikan fokus implementasi PRD v1.0. PRD v1.0 tetap jadi referensi untuk scope penuh & roadmap jangka panjang (termasuk Opsi A integrasi Moka).*
