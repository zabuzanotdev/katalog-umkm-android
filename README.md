# Zaa&Co — Katalog UMKM Android

Aplikasi Android **native** untuk katalog UMKM Zaa&Co, dibuat dari projek Laravel yang sudah di-deploy di **https://zaaxcoo.gt.tc**.

Tujuannya sederhana: pembeli bisa melihat katalog, mencari produk, memasukkan ke keranjang, checkout, lalu melanjutkan pesanan ke WhatsApp. Admin tetap memakai panel web yang sudah ada.

---

## Fitur

**Pembeli (tanpa login)**

- **Beranda**: hero, promo aktif, kategori, produk unggulan, produk terlaris, produk terbaru, dan stok habis. Tarik layar untuk refresh.
- **Katalog**: pencarian, filter kategori, urutkan (terbaru/termurah/termahal), pagination otomatis, dan bagian stok habis.
- **Detail produk**: gambar, harga promo, stok, pemilih jumlah, deskripsi, rating, ulasan, dan produk serupa.
- **Keranjang lokal**: tambah, ubah jumlah, hapus, kosongkan, subtotal dan total berubah realtime.
- **Checkout**: nama, nomor WhatsApp, alamat, catatan, ringkasan pesanan. Pesanan disimpan ke server, stok dikurangi, lalu WhatsApp penjual dibuka.
- **Lacak pesanan**: cek status dengan nomor pesanan + nomor WhatsApp, lihat riwayat status, item, total, dan kirim ulasan setelah pesanan selesai.
- **Tentang**: profil toko, alamat, jam buka, telepon, email, Instagram, Facebook, dan tombol WhatsApp.
- **Cache beranda**: data terakhir tetap tampil saat jaringan bermasalah.
- **Tema light** mengikuti palet web (maroon/coral/gold/cream/emerald), edge-to-edge, dan Material 3.

**Yang tidak ada di app**

- Login/admin panel (tetap di web).
- Payment gateway (memang bukan e-commerce penuh).
- Push notification.

---

## Palet warna

Mengikuti `tailwind.config.js` web:

| Peran | Warna |
|---|---|
| Primary | Maroon `#6B1E2A` |
| Secondary | Coral `#C0392B` |
| Accent | Gold `#F4A261` |
| Background | Cream `#FDF6F0` |
| Surface | White `#FFFFFF` |
| Text | Dark brown `#473122` |
| Muted | `#8A6A55` |
| Border | `#EFE1D5` |
| Sukses | Emerald `#2C6E4B` |

Web-nya light-only, jadi app juga light-only. Status bar memakai ikon gelap.

---

## APK siap install

| File | Ukuran | Keterangan |
|---|---:|---|
| `dist/ZaaCo-Katalog-v1.0.0-release.apk` | ± 7,4 MB | APK utama, signed debug agar langsung bisa di-install |
| `dist/ZaaCo-Katalog-v1.0.0-debug.apk` | ± 10 MB | Untuk debugging / uji coba |

- Minimal **Android 8.0** (API 26).
- Target **Android 15** (API 35).
- Package: `com.zaacoo.katalog`.
- APK **tidak ikut di-commit** (build artifact). Build sendiri dengan `assembleRelease`.

Cara install ada di `dist/CARA-INSTALL.md`.

---

## Penting: aktifkan API mobile dulu

Aplikasi membutuhkan file **`api.php`** di hosting. Tanpa file itu, app akan menampilkan pesan:

> API mobile belum aktif di server. Upload backend/api.php ke hosting, lalu coba lagi.

Langkah singkat:

1. Upload `backend/api.php` ke hosting:
   - **InfinityFree**: `htdocs/api.php` (sejajar `index.php`).
   - **cPanel**: `public_html/api.php` (sejajar `index.php`).
   - Kalau document root langsung ke folder `public`, upload ke `public/api.php`.
2. Tes di browser:
   `https://zaaxcoo.gt.tc/api.php?action=ping`
   Harus muncul:
   ```json
   {"ok":true,"time":"..."}
   ```
3. Buka app, tarik layar ke bawah di beranda untuk memuat data.

Detail lengkap: **`backend/README.md`**.

> File `api.php` tidak perlu migrate, route baru, atau composer. Dia memakai model dan service Laravel yang sudah ada.

---

## Build dari source

### Prasyarat

- JDK 21
- Android SDK dengan **platform 35** dan **build-tools 35**
- Gradle wrapper sudah disertakan (`gradlew.bat` / `gradlew`)

### Command line (Windows PowerShell)

```powershell
$env:JAVA_HOME = "C:\path\ke\jdk-21"
$env:ANDROID_HOME = "C:\Users\<user>\AppData\Local\Android\Sdk"

.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

Hasil:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

### Android Studio

1. Buka folder ini (`File > Open`).
2. Tunggu Gradle sync.
3. Pilih `app` > `Run`.

Jika Android Studio bertanya lokasi SDK, arahkan ke folder Android SDK. File `local.properties` dibuat otomatis dan tidak ikut di-commit.

---

## Ganti alamat server

Alamat website diatur di `gradle.properties`:

```properties
API_BASE_URL=https://zaaxcoo.gt.tc
```

Ganti nilainya, lalu rebuild. Bisa juga lewat command line:

```powershell
.\gradlew.bat :app:assembleRelease -PAPI_BASE_URL=https://domainbaru.com
```

Aplikasi memanggil:

```
<API_BASE_URL>/api.php?action=...
```

---

## Struktur project

```
app/src/main/java/com/zaacoo/katalog/
├── MainActivity.kt              # entry point + edge-to-edge
├── KatalogApplication.kt        # Application + container
├── AppContainer.kt              # manual dependency injection
├── data/
│   ├── Models.kt                # model JSON (kotlinx.serialization)
│   ├── ApiClient.kt             # OkHttp + JSON
│   ├── ChallengeInterceptor.kt  # solver cookie JS InfinityFree
│   ├── KatalogRepository.kt     # jembatan API <-> UI
│   ├── CartStore.kt             # keranjang lokal (SharedPreferences)
│   └── Prefs.kt                 # preferensi + cache beranda
├── ui/
│   ├── App.kt                   # navigation + bottom bar
│   ├── *ViewModel.kt            # state per layar
│   ├── screens/                 # Home, Catalog, Product, Cart, Checkout, Success, Track, About
│   ├── components/              # kartu produk, hero, dialog, komponen umum
│   └── theme/                   # warna, tipografi, shape
└── util/Format.kt               # Rupiah, tanggal, buka URL
```

Backend:

```
backend/api.php                  # endpoint JSON untuk app
backend/README.md                # panduan upload & endpoint
```

---

## Arsitektur

- **UI**: Jetpack Compose + Material 3, single activity, Navigation Compose.
- **State**: ViewModel + StateFlow, `collectAsStateWithLifecycle`.
- **Data**: repository + OkHttp + kotlinx.serialization.
- **Gambar**: Coil.
- **Keranjang**: lokal di `SharedPreferences` (tidak perlu login).
- **Cache**: snapshot beranda terakhir disimpan di `SharedPreferences`.
- **Keamanan**: HTTPS only, tanpa cleartext.

Alur checkout:

1. User mengisi data dan menekan **Pesan via WhatsApp**.
2. App mengirim `POST api.php?action=order` berisi `customer` + `items`.
3. Server memvalidasi stok, menghitung ulang harga dari database, membuat order + order items, dan mengurangi stok dalam satu transaksi.
4. Server mengembalikan order + `whatsapp_url`.
5. App mengosongkan keranjang, menampilkan halaman sukses, lalu membuka WhatsApp.
6. Nomor pesanan bisa dilacak di tab **Pesanan**.

Harga dan total **selalu dihitung ulang di server**; app tidak dipercaya sebagai sumber kebenaran.

---

## API yang dipakai

| Method | Endpoint | Fungsi |
|---|---|---|
| GET | `api.php?action=ping` | Tes koneksi |
| GET | `api.php?action=home` | Beranda (settings, kategori, promo, produk) |
| GET | `api.php?action=products&q=&category=&sort=&page=` | Katalog + pagination |
| GET | `api.php?action=product&slug=` | Detail produk + ulasan |
| GET | `api.php?action=categories` | Daftar kategori |
| GET | `api.php?action=settings` | Profil toko |
| GET | `api.php?action=track&order_number=&phone=` | Lacak pesanan |
| POST | `api.php?action=order` | Buat pesanan |
| POST | `api.php?action=review` | Kirim ulasan |

Detail payload ada di `backend/README.md`.

---

## Tantangan cookie JS (InfinityFree)

InfinityFree memasang tantangan JavaScript (`aes.js`) sebelum halaman PHP bisa diakses. Aplikasi native tidak punya mesin JS, jadi `ChallengeInterceptor` menyelesaikan tantangan yang sama:

1. Request pertama menerima halaman tantangan.
2. App mengambil nilai `a`, `b`, `c`.
3. App mendekripsi `c` dengan AES-128-CBC (key `a`, IV `b`) memakai `javax.crypto`.
4. Hasilnya dijadikan cookie `__test`, lalu request diulang.
5. Cookie disimpan agar request berikutnya tidak mengulang tantangan.

Ini bukan bypass keamanan: app melakukan hal yang sama seperti browser, hanya tanpa mesin JavaScript.

---

## Testing

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Tes aktif (5):

- Solver AES tantangan InfinityFree.
- Parsing `HomeResponse` (snake_case).
- Parsing `OrderResponse`.
- Encoding `CreateOrderRequest` (memastikan `product_id`, bukan `productId`).
- Penanganan HTML/404 sebagai error API yang ramah.

Tes live manual (2, di-ignore secara default):

- `LiveApiTest` mengecek `home` dan `products` langsung ke hosting.
- Jalankan:
  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.zaacoo.katalog.LiveApiTest"
  ```

Backend juga sudah dites lokal:

- `home`, `products`, `product`, `track`, `order`, `review`.
- Checkout mengurangi stok, membuat order, dan menghasilkan URL WhatsApp.
- Restock/cleanup data tes.

## Test run di perangkat

- Perangkat: **ZTE 9047**, Android **12 (API 31)**, arm64-v8a.
- APK debug & release berhasil di-install.
- Alur yang dicek:
  - Beranda: data live, logo, promo, kategori, cart badge di navbar.
  - Katalog: cari "kopi", filter Dessert, section stok habis, empty state + reset.
  - Detail produk: harga, stok, qty, tambah ke keranjang, snackbar.
  - Keranjang: ubah qty 1 → 2, total realtime Rp90.000, badge ikut berubah.
  - Checkout: form, ringkasan, validasi "Nama minimal 2 karakter."
  - Lacak pesanan: form + empty state.
  - Tentang: profil toko live, versi app, tombol WhatsApp.
  - WhatsApp: tombol membuka `com.whatsapp/.Conversation` (tanpa kirim pesan).
- Logcat bersih dari `FATAL EXCEPTION` / ANR.
- Checkout asli **tidak** di-submit saat tes supaya tidak membuat order nyata di hosting.

Screenshot hasil test ada di folder `test-artifacts/` (lokal, tidak ikut repo).

---

## Signing

APK release saat ini ditandatangani dengan **debug keystore** supaya bisa langsung di-install untuk demo/sidang.

Untuk Play Store:

1. Buat keystore sendiri:
   ```powershell
   keytool -genkeypair -v -keystore zaacoo-release.jks -alias zaacoo -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Tambahkan `signingConfigs` di `app/build.gradle.kts`.
3. Ganti `signingConfig = signingConfigs.getByName("debug")` menjadi konfigurasi release kamu.
4. Simpan keystore dan password baik-baik.

Jangan pakai debug keystore untuk aplikasi produksi.

---

## Troubleshooting

| Masalah | Penyebab | Solusi |
|---|---|---|
| "API mobile belum aktif" | `api.php` belum di-upload / path salah | Upload ke `htdocs/api.php` atau `public_html/api.php`, tes `?action=ping` |
| "Server mengirim halaman web" | File PHP error / tantangan tidak selesai | Cek `storage/logs/laravel.log`, pastikan PHP 8.3 dan `vendor/` ada |
| Gambar tidak muncul | URL storage salah / gambar tidak ada | Cek `https://domain/storage/...` di browser |
| WhatsApp tidak terbuka | WhatsApp tidak terpasang | Browser akan membuka `wa.me`; pasang WhatsApp atau buka lewat browser |
| Build gagal "SDK location not found" | `ANDROID_HOME` belum diset | Set `ANDROID_HOME` atau buat `local.properties` |
| Build gagal JDK | JDK terlalu baru/ lama | Pakai JDK 21 |
| Data beranda lama | Cache | Tarik layar ke bawah di beranda |
| Pesanan ditolak "stok tidak mencukupi" | Stok berubah sejak masuk keranjang | Kembali ke katalog, muat ulang, sesuaikan jumlah |

---

## Lisensi

GNU General Public License v3.0 atau lebih baru (GPL-3.0-or-later).
Copyright (C) 2026 zabuzanotdev.

Projek web aslinya ada di `D:\SEMESTER7\Katalog UMKM` dan repo GitHub `zabuzanotdev/katalog-umkm`.
