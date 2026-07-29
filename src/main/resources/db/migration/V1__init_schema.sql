-- ============================================================
-- V1: Skema awal MVP - Sistem Pemesanan Makanan dari Meja
-- Mengikuti Data Model MVP (PRD Bagian 6)
-- ============================================================

CREATE TABLE dining_table (
    table_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nomor_meja      VARCHAR(20)     NOT NULL,
    qr_token        VARCHAR(64)     NOT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_dining_table_qr_token UNIQUE (qr_token),
    CONSTRAINT uq_dining_table_nomor_meja UNIQUE (nomor_meja)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE menu_category (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nama_kategori   VARCHAR(100)    NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE menu_item (
    item_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code           VARCHAR(30)     NOT NULL,
    category_id         BIGINT          NOT NULL,
    nama                VARCHAR(150)    NOT NULL,
    harga               DECIMAL(12,2)   NOT NULL,
    deskripsi           VARCHAR(500),
    foto_url            VARCHAR(500),
    status_tersedia     TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_menu_item_code UNIQUE (item_code),
    CONSTRAINT fk_menu_item_category FOREIGN KEY (category_id) REFERENCES menu_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
    order_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code          VARCHAR(40)     NOT NULL,
    table_id            BIGINT          NOT NULL,
    status              VARCHAR(30)     NOT NULL,
    metode_pembayaran   VARCHAR(20)     NOT NULL,
    total_harga         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    synced_manual       TINYINT(1)      NOT NULL DEFAULT 0,
    synced_marked_by    VARCHAR(100),
    synced_marked_at    DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_orders_order_code UNIQUE (order_code),
    CONSTRAINT fk_orders_table FOREIGN KEY (table_id) REFERENCES dining_table (table_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);

CREATE TABLE order_item (
    order_item_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id            BIGINT          NOT NULL,
    item_id             BIGINT          NOT NULL,
    nama_item_snapshot  VARCHAR(150)    NOT NULL,
    harga_snapshot      DECIMAL(12,2)   NOT NULL,
    qty                 INT             NOT NULL,
    catatan             VARCHAR(300),
    subtotal            DECIMAL(12,2)   NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_menu_item FOREIGN KEY (item_id) REFERENCES menu_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment (
    payment_id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id                   BIGINT          NOT NULL,
    metode                     VARCHAR(20)     NOT NULL,
    midtrans_transaction_id    VARCHAR(100),
    midtrans_snap_token        VARCHAR(150),
    status                     VARCHAR(20)     NOT NULL,
    gross_amount                DECIMAL(12,2)  NOT NULL,
    paid_at                    DATETIME,
    confirmed_by               VARCHAR(100),
    expiry_time                DATETIME,
    created_at                 DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_payment_order_id ON payment (order_id);
CREATE INDEX idx_payment_midtrans_trx_id ON payment (midtrans_transaction_id);

CREATE TABLE cashier_log (
    log_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    aktor           VARCHAR(100)    NOT NULL,
    aksi            VARCHAR(50)     NOT NULL,
    keterangan      VARCHAR(500),
    timestamp       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cashier_log_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_cashier_log_order_id ON cashier_log (order_id);
