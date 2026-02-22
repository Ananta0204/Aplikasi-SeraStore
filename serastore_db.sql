-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Waktu pembuatan: 22 Feb 2026 pada 09.53
-- Versi server: 8.0.30
-- Versi PHP: 8.3.29

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Basis data: `serastore_db`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `activity_logs`
--

CREATE TABLE `activity_logs` (
  `id_log` int NOT NULL,
  `id_user` int DEFAULT NULL,
  `aksi` text,
  `waktu` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `activity_logs`
--

INSERT INTO `activity_logs` (`id_log`, `id_user`, `aksi`, `waktu`) VALUES
(1, 1, 'User login ke sistem', '2026-02-22 13:33:02'),
(2, NULL, 'Tambah barang: Vaselin', '2026-02-22 13:34:31'),
(3, NULL, 'Update barang: Vaselin', '2026-02-22 13:34:45'),
(4, 1, 'User login ke sistem', '2026-02-22 14:26:14'),
(5, 1, 'User login ke sistem', '2026-02-22 14:31:09'),
(6, 1, 'User login ke sistem', '2026-02-22 14:40:46'),
(7, 1, 'User login ke sistem', '2026-02-22 14:48:50'),
(8, 1, 'User login ke sistem', '2026-02-22 14:58:00'),
(9, 1, 'User login ke sistem', '2026-02-22 15:03:39'),
(10, 1, 'User login ke sistem', '2026-02-22 15:07:18'),
(11, 1, 'User login ke sistem', '2026-02-22 15:12:13'),
(12, 1, 'User login ke sistem', '2026-02-22 15:16:49'),
(13, 1, 'User login ke sistem', '2026-02-22 15:19:42'),
(14, 1, 'User login ke sistem', '2026-02-22 15:22:48'),
(15, 1, 'User login ke sistem', '2026-02-22 15:26:38'),
(16, 1, 'User login ke sistem', '2026-02-22 15:29:01'),
(17, 1, 'Berhasil Login', '2026-02-22 15:47:09'),
(18, 1, 'Berhasil Login', '2026-02-22 16:09:46'),
(19, NULL, 'Tambah: Rexona', '2026-02-22 16:11:31'),
(20, NULL, 'Edit: Vaselin', '2026-02-22 16:11:51'),
(21, NULL, 'Edit: Rexona', '2026-02-22 16:12:04');

-- --------------------------------------------------------

--
-- Struktur dari tabel `categories`
--

CREATE TABLE `categories` (
  `id_kategori` int NOT NULL,
  `nama_kategori` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `categories`
--

INSERT INTO `categories` (`id_kategori`, `nama_kategori`) VALUES
(1, 'Umum'),
(2, 'Makanan'),
(3, 'Minuman'),
(4, 'Elektronik'),
(5, 'Fashion');

-- --------------------------------------------------------

--
-- Struktur dari tabel `products`
--

CREATE TABLE `products` (
  `id_produk` int NOT NULL,
  `barcode` varchar(50) NOT NULL,
  `nama_produk` varchar(100) NOT NULL,
  `id_kategori` int DEFAULT NULL,
  `harga_beli` double DEFAULT NULL,
  `harga_jual` double DEFAULT NULL,
  `stok` int DEFAULT NULL,
  `stok_min` int DEFAULT '5',
  `satuan` varchar(20) DEFAULT NULL,
  `gambar_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `products`
--

INSERT INTO `products` (`id_produk`, `barcode`, `nama_produk`, `id_kategori`, `harga_beli`, `harga_jual`, `stok`, `stok_min`, `satuan`, `gambar_path`) VALUES
(1, '8999999502942', 'Vaselin', 5, 100000, 1100000, 0, 5, '111', 'D:\\Pribadi\\Projek\\PBO\\SeraStore\\product_images\\IMG_1771742083496.jpg'),
(2, '8999999580773', 'Rexona', 5, 50000, 55000, 8, 5, '10', 'D:\\Pribadi\\Projek\\PBO\\SeraStore\\product_images\\IMG_1771751488371.jpg');

-- --------------------------------------------------------

--
-- Struktur dari tabel `returns`
--

CREATE TABLE `returns` (
  `id_retur` int NOT NULL,
  `no_faktur` varchar(50) DEFAULT NULL,
  `id_produk` int DEFAULT NULL,
  `qty_retur` int DEFAULT NULL,
  `alasan` text,
  `tgl_retur` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `transactions`
--

CREATE TABLE `transactions` (
  `id_transaksi` int NOT NULL,
  `no_faktur` varchar(50) DEFAULT NULL,
  `tgl_transaksi` datetime DEFAULT CURRENT_TIMESTAMP,
  `id_user` int DEFAULT NULL,
  `total_kotor` double DEFAULT NULL,
  `total_diskon` double DEFAULT '0',
  `total_akhir` double DEFAULT NULL,
  `bayar` double DEFAULT NULL,
  `kembali` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `transactions`
--

INSERT INTO `transactions` (`id_transaksi`, `no_faktur`, `tgl_transaksi`, `id_user`, `total_kotor`, `total_diskon`, `total_akhir`, `bayar`, `kembali`) VALUES
(1, 'TRX-1771746093558', '2026-02-22 14:41:33', NULL, NULL, 0, 110, 1111, 1001),
(2, 'TRX-1771746672662', '2026-02-22 14:51:12', NULL, NULL, 0, 222, 11111, 10889),
(3, 'TRX-1771747126723', '2026-02-22 14:58:46', NULL, NULL, 0, 101, 1011, 910),
(4, 'TRX-1771747489912', '2026-02-22 15:04:49', NULL, NULL, 0, 110, 111, 1),
(5, 'TRX-1771747740630', '2026-02-22 15:09:00', NULL, NULL, 0, 110, 111, 1),
(6, 'TRX-1771751583334', '2026-02-22 16:13:03', NULL, NULL, 0, 210000, 250000, 40000);

-- --------------------------------------------------------

--
-- Struktur dari tabel `transaction_details`
--

CREATE TABLE `transaction_details` (
  `id_detail` int NOT NULL,
  `no_faktur` varchar(50) DEFAULT NULL,
  `id_produk` int DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `harga_satuan` double DEFAULT NULL,
  `subtotal` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `transaction_details`
--

INSERT INTO `transaction_details` (`id_detail`, `no_faktur`, `id_produk`, `qty`, `harga_satuan`, `subtotal`) VALUES
(1, 'TRX-1771747126723', 1, 1, NULL, 111),
(2, 'TRX-1771747489912', 1, 1, NULL, 111),
(3, 'TRX-1771747740630', 1, 1, NULL, 111),
(4, 'TRX-1771751583334', 2, 2, NULL, 110000),
(5, 'TRX-1771751583334', 1, 1, NULL, 1100000);

-- --------------------------------------------------------

--
-- Struktur dari tabel `users`
--

CREATE TABLE `users` (
  `id_user` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama_lengkap` varchar(100) DEFAULT NULL,
  `role` enum('Admin','Kasir') NOT NULL,
  `last_login` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `users`
--

INSERT INTO `users` (`id_user`, `username`, `password`, `nama_lengkap`, `role`, `last_login`) VALUES
(1, 'admin', 'admin123', 'Owner Sera Store', 'Admin', NULL),
(2, 'kasir', 'kasir123', 'Staff Kasir 1', 'Kasir', NULL);

--
-- Indeks untuk tabel yang dibuang
--

--
-- Indeks untuk tabel `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD PRIMARY KEY (`id_log`),
  ADD KEY `id_user` (`id_user`);

--
-- Indeks untuk tabel `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id_kategori`);

--
-- Indeks untuk tabel `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id_produk`),
  ADD UNIQUE KEY `barcode` (`barcode`),
  ADD KEY `id_kategori` (`id_kategori`);

--
-- Indeks untuk tabel `returns`
--
ALTER TABLE `returns`
  ADD PRIMARY KEY (`id_retur`),
  ADD KEY `no_faktur` (`no_faktur`);

--
-- Indeks untuk tabel `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`id_transaksi`),
  ADD UNIQUE KEY `no_faktur` (`no_faktur`),
  ADD KEY `id_user` (`id_user`);

--
-- Indeks untuk tabel `transaction_details`
--
ALTER TABLE `transaction_details`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `no_faktur` (`no_faktur`),
  ADD KEY `id_produk` (`id_produk`);

--
-- Indeks untuk tabel `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `activity_logs`
--
ALTER TABLE `activity_logs`
  MODIFY `id_log` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT untuk tabel `categories`
--
ALTER TABLE `categories`
  MODIFY `id_kategori` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT untuk tabel `products`
--
ALTER TABLE `products`
  MODIFY `id_produk` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT untuk tabel `returns`
--
ALTER TABLE `returns`
  MODIFY `id_retur` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `transactions`
--
ALTER TABLE `transactions`
  MODIFY `id_transaksi` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT untuk tabel `transaction_details`
--
ALTER TABLE `transaction_details`
  MODIFY `id_detail` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT untuk tabel `users`
--
ALTER TABLE `users`
  MODIFY `id_user` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Ketidakleluasaan untuk tabel pelimpahan (Dumped Tables)
--

--
-- Ketidakleluasaan untuk tabel `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD CONSTRAINT `activity_logs_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

--
-- Ketidakleluasaan untuk tabel `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`id_kategori`) REFERENCES `categories` (`id_kategori`);

--
-- Ketidakleluasaan untuk tabel `returns`
--
ALTER TABLE `returns`
  ADD CONSTRAINT `returns_ibfk_1` FOREIGN KEY (`no_faktur`) REFERENCES `transactions` (`no_faktur`);

--
-- Ketidakleluasaan untuk tabel `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

--
-- Ketidakleluasaan untuk tabel `transaction_details`
--
ALTER TABLE `transaction_details`
  ADD CONSTRAINT `transaction_details_ibfk_1` FOREIGN KEY (`no_faktur`) REFERENCES `transactions` (`no_faktur`) ON DELETE CASCADE,
  ADD CONSTRAINT `transaction_details_ibfk_2` FOREIGN KEY (`id_produk`) REFERENCES `products` (`id_produk`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
