# Business Requirements Document (BRD) — MVP
## Sistem Pemesanan Makanan dari Meja

**Versi:** 2.0 (MVP)
**Tanggal:** 29 Juli 2026
**Status:** Draft
**Referensi:** BRD v1.0 (full scope)

---

## 1. Pendahuluan

### 1.1 Latar Belakang
Sama seperti versi sebelumnya: proses order manual via pelayan/kasir rentan antrian, human error, dan lambat sampai ke dapur. Dokumen ini mempersempit scope menjadi **MVP** — fitur inti minimum yang bisa langsung dipakai operasional tanpa bergantung pada proses yang di luar kendali kita (misalnya approval API partner dari vendor POS).

### 1.2 Tujuan MVP
- Validasi konsep self-order dari meja dengan effort development seminimal mungkin.
- Pelanggan bisa order & bayar (QRIS via Midtrans atau bayar di kasir) tanpa bantuan pelayan.
- Kasir bisa menerima & memproses order secara real-time meski POS existing (Moka) belum terintegrasi langsung.
- Punya fondasi arsitektur yang gampang di-upgrade ke Opsi A (direct API ke Moka) begitu akses partner didapat, tanpa bongkar ulang sistem inti.

### 1.3 Ruang Lingkup MVP

**Termasuk:**
- Web app pemesanan pelanggan (scan QR per meja → menu → cart → checkout).
- Pembayaran QRIS via Midtrans (Snap API).
- Pembayaran manual di kasir.
- Companion Screen kasir: menampilkan order masuk real-time, konfirmasi pembayaran cash, update status order.
- Order ID unik yang konsisten di semua sisi (customer app, Midtrans, companion screen).
- Manajemen menu dasar (CRUD sederhana oleh admin — nama, harga, kategori, foto, status tersedia).

**Tidak termasuk (didorong ke fase berikutnya):**
- Integrasi direct API ke Moka (Opsi A / Advanced Ordering API).
- E-wallet selain QRIS (GoPay/OVO/Dana sebagai channel terpisah — bisa menyusul jika effort-nya kecil, tapi bukan syarat rilis MVP).
- Cetak struk otomatis ke printer thermal.
- Laporan/analytics, rekap transaksi harian yang kompleks (cukup daftar transaksi mentah untuk MVP).
- Kitchen Display System.
- Fitur "panggil pelayan".
- Notifikasi WA/SMS ke pelanggan.
- Multi-outlet.

---

## 2. Business Objectives MVP

| No | Objective | Indikator Keberhasilan |
|----|-----------|-------------------------|
| 1 | Validasi alur self-order end-to-end (pesan → bayar → sampai ke kasir) | Sistem berhasil dipakai operasional harian tanpa order hilang/gagal sync |
| 2 | Kurangi ketergantungan pada integrasi pihak ketiga yang belum pasti | MVP tetap bisa jalan penuh meski akses Advanced Ordering API Moka belum disetujui |
| 3 | Waktu rilis secepat mungkin | MVP live dalam target waktu pengembangan yang disepakati (bukan fitur lengkap) |

---

## 3. Stakeholder (tidak berubah dari versi full)

| Role | Kepentingan |
|------|-------------|
| Pelanggan | Order & bayar mandiri dari meja |
| Kasir | Terima & proses order via Companion Screen, konfirmasi pembayaran cash |
| Admin/Manager | Kelola menu, pantau order berjalan |

*(Waiter, tim dapur formal, dan role lain yang butuh fitur khusus di luar MVP belum relevan di tahap ini.)*

---

## 4. Proses Bisnis MVP (To-Be, ringkas)

1. Pelanggan scan QR meja → lihat menu → checkout → pilih bayar QRIS (Midtrans) atau bayar di kasir.
2. Order dengan `order_id` unik tercatat di sistem.
3. Order muncul real-time di **Companion Screen** kasir.
4. Untuk QRIS: status otomatis `PAID` setelah notifikasi Midtrans diterima. Untuk bayar kasir: kasir konfirmasi manual saat pelanggan bayar di tempat.
5. Kasir input ulang order ke Moka POS existing berdasarkan tampilan di Companion Screen (double-entry — disadari sebagai trade-off MVP), lalu tandai order sebagai "sudah diinput" di Companion Screen.
6. Pesanan diproses & diantar ke meja.

---

## 5. Business Requirements MVP

| ID | Kebutuhan Bisnis | Prioritas |
|----|-------------------|-----------|
| BR-01 | Pelanggan dapat memesan dari meja tanpa bantuan pelayan | Wajib |
| BR-02 | Sistem dapat mengidentifikasi meja pelanggan otomatis via QR unik | Wajib |
| BR-03 | Pelanggan dapat bayar via QRIS (Midtrans) atau bayar di kasir | Wajib |
| BR-04 | Setiap order punya `order_id` unik terlepas dari metode pembayaran | Wajib |
| BR-05 | Order muncul real-time di Companion Screen kasir | Wajib |
| BR-06 | Kasir dapat konfirmasi manual pembayaran cash | Wajib |
| BR-07 | Kasir dapat menandai order sebagai "sudah diinput ke Moka" untuk mencegah double-entry tanpa sadar | Wajib |
| BR-08 | Admin dapat mengelola data menu (tambah/edit/nonaktifkan item) | Wajib |

---

## 6. Asumsi & Batasan MVP

**Asumsi:**
- Outlet single lokasi, koneksi internet stabil.
- QR code per meja sudah/akan dicetak sebelum rilis.
- Akun Midtrans merchant sudah tersedia.
- Kasir bersedia melakukan input manual ganda ke Moka selama Opsi A belum tersedia — ini diterima sebagai keterbatasan sementara, bukan solusi akhir.

**Batasan:**
- MVP TIDAK terintegrasi langsung ke Moka POS — status "order selesai diproses di POS" sepenuhnya bergantung kedisiplinan kasir menandai di Companion Screen.
- Rekonsiliasi antara sistem MVP dan Moka POS dilakukan manual selama fase ini.

---

## 7. Success Metrics MVP

- Sistem berhasil menangani order end-to-end (dari checkout sampai tampil di Companion Screen) tanpa order hilang, dalam masa uji coba operasional minimal 2 minggu.
- Tingkat keberhasilan sinkronisasi status pembayaran Midtrans ≥ 99%.
- Kasir dapat mengoperasikan Companion Screen tanpa training lebih dari 15 menit.

---

## 8. Risiko MVP

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| Kasir lupa input order ke Moka POS (karena double-entry manual) | Pesanan tidak tercatat di POS meski sudah dibayar | Highlight visual order yang belum ditandai "sudah diinput" di Companion Screen |
| Selisih pencatatan antara sistem MVP dan Moka POS | Kesulitan rekonsiliasi harian | Companion Screen menyimpan log lengkap sebagai sumber kebenaran sekunder saat rekonsiliasi manual |
| Koneksi internet terputus | Pelanggan gagal order/bayar | Fallback: pelanggan tetap bisa datang langsung ke kasir seperti alur lama |

---

## 9. Roadmap Setelah MVP (Non-Binding, untuk Konteks)

1. Ajukan akses **Advanced Ordering API** ke Moka Connect → migrasi dari Companion Screen ke Opsi A (direct integration).
2. Tambah channel e-wallet lain di Midtrans.
3. Cetak struk otomatis.
4. Laporan & rekonsiliasi otomatis.
5. Fitur panggil pelayan, notifikasi WA/SMS.

---

*Dokumen ini menggantikan fokus BRD v1.0 untuk kebutuhan rilis awal (MVP). BRD v1.0 tetap menjadi referensi scope penuh untuk roadmap jangka panjang.*
