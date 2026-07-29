# Sistem Pemesanan Makanan dari Meja

Sistem pemesanan makanan dari meja yang memungkinkan pelanggan memesan langsung lewat scan QR code di meja tanpa bantuan pelayan. Mendukung dua metode pembayaran — QRIS/e-wallet via **Midtrans** atau bayar langsung di kasir — dengan setiap order yang tersinkronisasi real-time ke sistem kasir untuk diproses. Dibangun bertahap dimulai dari MVP (Companion Screen kasir) menuju integrasi POS penuh.

## Fitur

- Pemesanan mandiri dari meja via scan QR code
- Pembayaran QRIS/e-wallet terintegrasi Midtrans
- Opsi bayar langsung di kasir
- Sinkronisasi order real-time ke sistem kasir
- Manajemen menu dasar

## Branching Strategy

Repo ini pakai dua jenis branch dengan tujuan berbeda:

### `main`
- Branch **production** — kode yang ada di sini adalah kode yang sudah rilis/live.
- Tidak boleh di-push langsung. Semua perubahan wajib masuk lewat **Pull Request** dari branch development, minimal 1 approval, dan status checks harus lolos.
- Dilindungi lewat GitHub Ruleset (`ruleset-main.json`) — block force push, block delete, require PR.

### `development/<nama-fitur>`
- Branch tempat kerja aktif untuk tiap fitur/perbaikan, dibuat dari `main`.
- Format penamaan: `development/<nama-fitur>`, contoh:
  - `development/checkout-qris`
  - `development/companion-screen`
  - `development/midtrans-webhook`
  - `development/menu-management`
- Bebas push langsung selama masih dalam tahap pengembangan (dilindungi minimal dari force-push & delete lewat `ruleset-development.json`).
- Setelah fitur selesai & teruji, buka Pull Request dari `development/<nama-fitur>` ke `main` untuk direview sebelum di-merge.

### Alur singkat
```
main
 └── development/checkout-qris   (kerja fitur checkout)
       └── selesai → PR ke main → review → merge → rilis
 └── development/companion-screen (kerja fitur companion screen)
       └── selesai → PR ke main → review → merge → rilis
```

## Payment Gateway

Menggunakan **Midtrans** (Snap API) untuk channel QRIS/e-wallet, dengan webhook notification handler untuk update status pembayaran secara real-time.