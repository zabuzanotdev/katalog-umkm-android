# Cara Install Zaa&Co Katalog UMKM

## Yang dibutuhkan

- HP Android **8.0 ke atas** (API 26+).
- Koneksi internet.
- WhatsApp (untuk melanjutkan pesanan).
- File APK: `ZaaCo-Katalog-v1.0.0-release.apk`.

---

## Langkah 1 — Aktifkan API mobile

App butuh `api.php` di hosting. Kalau belum, app akan menampilkan pesan **"API mobile belum aktif"**.

1. Upload `api.php` ke hosting:
   - **InfinityFree**: `htdocs/api.php`
   - **cPanel**: `public_html/api.php`
2. Buka di browser:
   ```
   https://zaaxcoo.gt.tc/api.php?action=ping
   ```
3. Kalau muncul `{"ok":true,...}`, API sudah aktif.

Panduan lengkap: `backend/README.md`.

---

## Langkah 2 — Pindahkan APK ke HP

Pilih salah satu:

- **USB**: colok HP, copy APK ke folder `Download`.
- **Google Drive**: upload, lalu download di HP.
- **WhatsApp/Telegram**: kirim ke diri sendiri, lalu buka di HP.

---

## Langkah 3 — Install

1. Buka file APK dari file manager / notifikasi download.
2. Android akan meminta izin **"Install unknown apps"**.
3. Pilih **Settings** → izinkan aplikasi file manager / browser yang kamu pakai.
4. Kembali, tekan **Install**.
5. Tunggu sampai selesai, lalu tekan **Open**.

Kalau muncul **"App not installed"**:

- Uninstall versi lama dengan package `com.zaacoo.katalog` dulu.
- Pastikan ruang penyimpanan cukup (± 30 MB).
- Pastikan APK tidak rusak (download ulang).

---

## Langkah 4 — Buka app

1. App akan memuat beranda dari `https://zaaxcoo.gt.tc`.
2. Kalau data muncul, API sudah terhubung.
3. Kalau muncul error, tekan **Coba lagi**; kalau tetap gagal, cek Langkah 1.

Yang bisa langsung dicoba:

- Geser promo dan kategori di beranda.
- Buka **Produk**, cari nama produk, filter kategori.
- Buka detail produk, atur jumlah, tekan **Tambah**.
- Buka **Keranjang**, ubah jumlah, tekan **Checkout**.
- Isi data, tekan **Pesan via WhatsApp**.
- Setelah sukses, buka **Pesanan** untuk melacak.

---

## Catatan penting

- **Keranjang disimpan di HP**, bukan di server. Kalau app di-uninstall / data dibersihkan, keranjang hilang.
- **Pesanan tersimpan di server** setelah checkout, jadi masih bisa dilacak dari HP lain dengan nomor pesanan + nomor WhatsApp.
- **Harga dan stok dihitung ulang di server** saat checkout. Kalau stok berubah, app akan menampilkan pesan dan kamu bisa menyesuaikan jumlah.
- **APK release ini ditandatangani dengan debug keystore** supaya bisa langsung di-install untuk demo/sidang. Untuk Play Store, build ulang dengan keystore sendiri (lihat `README.md` bagian Signing).
- App memakai HTTPS; tidak ada koneksi cleartext.

---

## Spesifikasi teknis

| Item | Nilai |
|---|---|
| Nama app | Zaa&Co |
| Package | `com.zaacoo.katalog` |
| Versi | 1.0.0 (versionCode 1) |
| Min Android | 8.0 (API 26) |
| Target Android | 15 (API 35) |
| Ukuran release | ± 7,4 MB |
| Arsitektur | Native Kotlin + Jetpack Compose |
| Backend | Laravel di `https://zaaxcoo.gt.tc` |

---

## Uninstall

1. Tekan lama ikon app → **App info** → **Uninstall**.
2. Atau **Settings → Apps → Zaa&Co → Uninstall**.

Data lokal (keranjang, nama/alamat terakhir, cookie sesi) ikut terhapus. Data pesanan di server tetap aman.
