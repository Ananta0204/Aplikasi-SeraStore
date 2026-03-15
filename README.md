
# 🛒 SERA STORE - Professional POS System v1.0

![GitHub stars](https://img.shields.io/github/stars/Ananta0204/Aplikasi-SeraStore?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)

**Sera Store** adalah aplikasi kasir (Point of Sale) modern berbasis Desktop yang dirancang khusus untuk efisiensi bisnis retail. Dibangun dengan **Pure Java Classes** (Tanpa GUI Designer) untuk kontrol penuh atas performa dan estetika UI yang mewah.

---

## 📽️ Demo Aplikasi
> **Note:** Karena ukuran video yang cukup besar, silakan tonton demo lengkap fitur melalui link di bawah ini:

[▶️ KLIK DI SINI UNTUK NONTON VIDEO DEMO SERA STORE](https://github.com/Ananta0204/Aplikasi-SeraStore/releases/download/DemoApp/ZAZAZAZA.mp4))

---

## ✨ Fitur Unggulan

Aplikasi ini bukan sekadar pencatat transaksi, tapi sistem manajemen toko yang lengkap:

### 🛡️ Role-Based Access Control (RBAC)
*   **Multi-user Support:** Login sebagai Admin atau Kasir.
*   **Security:** Kasir tidak dapat melihat laporan keuangan, log aktivitas, atau pengaturan user. Hanya menu operasional yang dimunculkan.

### 📦 Smart Inventory Management
*   **Scan Barcode:** Input barang cepat menggunakan kamera (DroidCam support).
*   **Auto-Crop Image:** Fitur cerdas memotong foto produk menjadi Square (1:1) secara otomatis untuk tampilan POS yang presisi.
*   **Low Stock Alert:** Notifikasi otomatis di dashboard untuk produk yang hampir habis.

### 💰 Point of Sale (POS)
*   **Real-time Logic:** Perhitungan kembalian dan total belanja berjalan otomatis setiap karakter diketik.
*   **Double Discount System:** Support diskon per item (tabel) dan diskon global (total belanja).
*   **Manual Input:** Fitur input barcode manual jika hardware kamera bermasalah.

### 📊 Professional Analytics & Reporting
*   **Modern Dashboard:** Grafik pendapatan mingguan berbasis Bar Chart.
*   **PDF Export:** Generate struk belanja dan laporan penjualan periode tertentu ke file PDF yang rapi menggunakan iText.
*   **Audit Log:** Mencatat setiap aktivitas user (Login, tambah barang, transaksi) untuk keamanan owner.

---

## 🎨 UI/UX Philosophy
*   **Maroon Luxury Theme:** Kombinasi warna Maroon (#800000) dan Charcoal untuk kesan profesional.
*   **Flat & Semi-Rounded:** Menggunakan **FlatLaf 3.7** dengan `arc: 20` untuk tampilan yang modern dan tidak kaku.
*   **Dark Mode Toggle:** Mendukung transisi tema gelap/terang secara instan dengan efek animasi halus.

---

## 🛠️ Teknologi & Library
*   **JDK 25** (Platform Utama)
*   **FlatLaf** (Modern UI Theme)
*   **Sarxos Webcam API** (Hardware Camera Connectivity)
*   **ZXing (Zebra Crossing)** (Barcode Processing Engine)
*   **iText 2.1.7** (PDF Report Generator)
*   **JFreeChart** (Data Visualization)
*   **MySQL Connector** (Database Driver)

---

## 🚀 Instalasi & Persiapan

1.  **Clone Repository:**
    ```bash
    git clone https://github.com/Ananta0204/Aplikasi-SeraStore.git
    ```
2.  **Import Database:**
    *   Buka phpMyAdmin.
    *   Buat database bernama `serastore_db`.
    *   Import file `serastore_db.sql` yang ada di root folder.
3.  **Library Setup:**
    *   Pastikan semua library `.jar` di folder `lib` sudah ditambahkan ke Project Libraries di IDE anda (NetBeans/IntelliJ).
4.  **Run Application:**
    *   Jalankan file `Main.java`.
    *   Login Default: `admin` / `admin123`.

---

## 👨‍💻 Developer
**Ananta** - *Full Stack Desktop Developer*

"Building smart solutions for a better business experience."
