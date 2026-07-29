# Table Order MVP — Backend

Backend Spring Boot + MySQL untuk **Sistem Pemesanan Makanan dari Meja (MVP)**.
Diimplementasikan mengikuti:
- `BRD MVP v2.0`
- `PRD MVP v2.0`
- `API Contract MVP v1.0`

## Tech Stack

- Java 17
- Spring Boot 3.3.2 (Web, Data JPA, Validation, WebFlux — dipakai hanya untuk `WebClient` ke API Midtrans, bukan reactive stack penuh)
- MySQL 8
- Flyway (migrasi skema otomatis saat aplikasi start)
- Lombok

## Struktur Project

```
src/main/java/com/tableorder/
├── entity/            # JPA entities (Table, MenuCategory, MenuItem, Order, OrderItem, Payment, CashierLog)
├── entity/enums/       # OrderStatus, PaymentMethod, PaymentStatus, CashierAction
├── repository/         # Spring Data JPA repositories
├── service/            # Interface service
├── service/impl/       # Implementasi service + scheduler
├── controller/         # REST controllers
├── dto/request/        # Request DTO
├── dto/response/        # Response DTO
├── exception/          # Custom exception + GlobalExceptionHandler
├── util/               # OrderCodeGenerator, MidtransSignatureValidator
└── config/              # MidtransProperties, CORS, WebClient bean

src/main/resources/
├── application.yml
└── db/migration/       # Flyway: V1__init_schema.sql, V2__seed_data.sql
```

## Menjalankan Secara Lokal

### 1. Siapkan MySQL

Opsi termudah, pakai Docker:

```bash
docker compose up -d
```

Ini akan menjalankan MySQL 8 di `localhost:3306` dengan database `tableorder_db`,
user `tableorder_user` / password `changeme` (lihat `docker-compose.yml`).

Atau kalau sudah punya MySQL sendiri, buat database & user manual:

```sql
CREATE DATABASE tableorder_db CHARACTER SET utf8mb4;
CREATE USER 'tableorder_user'@'%' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON tableorder_db.* TO 'tableorder_user'@'%';
FLUSH PRIVILEGES;
```

Skema tabel (`V1__init_schema.sql`) dan data contoh (`V2__seed_data.sql`) akan otomatis
dijalankan oleh Flyway saat aplikasi start — tidak perlu import manual.

### 2. Set Environment Variables (Midtrans)

Daftar/masuk ke [Midtrans Dashboard Sandbox](https://dashboard.sandbox.midtrans.com/), ambil **Server Key** & **Client Key**, lalu:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=tableorder_db
export DB_USERNAME=tableorder_user
export DB_PASSWORD=changeme

export MIDTRANS_SERVER_KEY=SB-Mid-server-xxxxxxxxxxxxx
export MIDTRANS_CLIENT_KEY=SB-Mid-client-xxxxxxxxxxxxx
# default sudah mengarah ke sandbox; untuk production ganti:
# export MIDTRANS_BASE_URL=https://app.midtrans.com
# export MIDTRANS_API_BASE_URL=https://api.midtrans.com
```

Semua variabel di atas punya default di `application.yml` (kecuali server/client key wajib diisi
sebelum flow QRIS bisa berfungsi betulan).

### 3. Build & Run

```bash
mvn clean spring-boot:run
```

atau

```bash
mvn clean package -DskipTests
java -jar target/table-order-mvp-1.0.0.jar
```

Server jalan di `http://localhost:8080`.

### 4. Webhook Midtrans (Notification URL)

Untuk testing lokal, expose port 8080 pakai ngrok/cloudflared, lalu set
**Payment Notification URL** di dashboard Midtrans Sandbox ke:

```
https://<ngrok-url>/api/payments/midtrans/notification
```

---

## Endpoint yang Tersedia

### Sesuai API Contract MVP (persis)

| Method | Path | Keterangan |
|--------|------|------------|
| POST | `/api/orders` | Customer checkout (QRIS/Cash) |
| POST | `/api/payments/midtrans/notification` | Webhook Midtrans |
| GET | `/api/cashier/orders?status=active` | Daftar order di Companion Screen |
| POST | `/api/cashier/orders/{order_id}/confirm-payment` | Konfirmasi bayar cash |
| POST | `/api/cashier/orders/{order_id}/mark-synced` | Tandai sudah diinput ke Moka |
| PATCH | `/api/cashier/orders/{order_id}/status` | Update status (DIPROSES/SIAP/SELESAI) |
| GET | `/api/orders/{order_id}/status` | Polling status dari sisi customer |

### Tambahan (mendukung FR-02, FR-18, FR-19 — belum ada contoh JSON eksplisit di API Contract)

| Method | Path | Keterangan |
|--------|------|------------|
| GET | `/api/menu/categories` | Browse kategori menu (customer) |
| GET | `/api/menu/items` | Browse item menu yang tersedia (customer) |
| GET | `/api/admin/menu-categories` | List kategori (admin) |
| POST | `/api/admin/menu-categories` | Tambah kategori |
| GET | `/api/admin/menu-items` | List semua item (admin, termasuk nonaktif) |
| POST | `/api/admin/menu-items` | Tambah item menu |
| PUT | `/api/admin/menu-items/{itemId}` | Edit item menu |
| PATCH | `/api/admin/menu-items/{itemId}/availability` | Aktif/nonaktifkan item — body `{"status_tersedia": true}` |
| GET | `/api/admin/tables` | List meja |
| POST | `/api/admin/tables` | Tambah meja (qr_token auto-generate kalau kosong) |
| PATCH | `/api/admin/tables/{tableId}/active` | Aktif/nonaktifkan meja — body `{"is_active": true}` |

> Path `/api/admin/**` di MVP ini **belum diamankan dengan autentikasi** (lihat Open Question #3
> di PRD: apakah butuh login terpisah admin vs kasir). Tambahkan Spring Security
> sebelum go-live produksi.

---

## Catatan Implementasi Penting

- **Order state machine** mengikuti persis PRD Bagian 5: `PENDING_PAYMENT → PAID → DIPROSES → SIAP → SELESAI`,
  dengan cabang `EXPIRED`/`FAILED` dari `PENDING_PAYMENT`. Validasi transisi ada di `CashierServiceImpl`.
- **`synced_manual`** adalah flag terpisah dari `status` utama (BR-07/FR-14/FR-15), dipakai untuk
  mencegah double-entry manual ke Moka POS.
- **Snapshot harga & nama item** disimpan di `order_item` (`nama_item_snapshot`, `harga_snapshot`)
  supaya histori order tidak berubah kalau menu diedit setelahnya.
- **Signature Midtrans** divalidasi dengan formula resmi `SHA512(order_id + status_code + gross_amount + ServerKey)`.
  Request dengan signature tidak valid dibalas `403` dengan body `{"status": "invalid_signature"}` — bukan retry.
- **`OrderCodeGenerator`** memakai counter in-memory per hari (format `ORD-YYYYMMDD-NNNNNN`).
  Ini cukup untuk MVP single-instance; kalau nanti backend di-scale ke multi-instance,
  ganti dengan sequence berbasis DB agar tidak ada tabrakan `order_code`.
- **Jalur upgrade ke Opsi A** (PRD Bagian 8): kolom `synced_manual` sudah ada di skema, dan
  struktur data (`Order`, `Payment`) sudah dirancang agar tinggal ditambah field
  `external_pos_order_ref` + service baru yang subscribe event `PAID`, tanpa redesain tabel.
- **CORS** dibuka untuk semua origin (`WebConfig`) karena customer app diakses dari HP pelanggan
  dengan domain yang bervariasi. Persempit ke domain resmi sebelum produksi.

## Yang Belum Termasuk (sesuai scope MVP di BRD/PRD)

- Autentikasi/otorisasi (role admin vs kasir)
- E-wallet selain QRIS, cetak struk otomatis, integrasi direct Moka API, notifikasi WA/SMS
- Unit test & integration test otomatis (disarankan ditambahkan sebelum rilis produksi)
