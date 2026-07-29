-- ============================================================
-- V2: Data awal untuk development/testing
-- ============================================================

INSERT INTO dining_table (nomor_meja, qr_token, is_active) VALUES
    ('07', 'TBL-QR-000007', 1),
    ('12', 'TBL-QR-000012', 1);

INSERT INTO menu_category (nama_kategori, sort_order) VALUES
    ('Makanan Utama', 1),
    ('Minuman', 2);

INSERT INTO menu_item (item_code, category_id, nama, harga, deskripsi, foto_url, status_tersedia) VALUES
    ('MENU-045', 1, 'Nasi Goreng Spesial', 25000, 'Nasi goreng dengan telur & ayam suwir', NULL, 1),
    ('MENU-046', 1, 'Ayam Bakar', 35000, 'Ayam bakar bumbu kecap', NULL, 1),
    ('MENU-102', 2, 'Es Teh Manis', 8000, 'Teh manis dingin', NULL, 1);
