# API Mobile Katalog UMKM

`api.php` adalah jembatan JSON kecil untuk aplikasi Android Zaa&Co. File ini **tidak** menambah tabel, route, atau dependency baru. Dia bootstrap Laravel yang sudah ada, lalu memakai model dan service yang sama dengan website:

- `App\Models\Product`, `Category`, `Promotion`, `Order`, `BusinessSetting`, `Review`
- `App\Services\WhatsAppService`
- `Illuminate\Support\Facades\DB`, `Cache`, `Log`

Artinya: produk, harga, promo, stok, dan pesanan yang tampil di app **sama persis** dengan website.

---

## 1. Upload

Pilih sesuai struktur hosting.

### InfinityFree (paling umum)

Struktur yang dipakai projek ini:

```
htdocs/
├── index.php
├── api.php              <-- upload file ini
├── build/
├── storage/
└── katalog-umkm/        <-- aplikasi Laravel
    ├── app/
    ├── bootstrap/
    ├── vendor/
    └── ...
```

Upload `api.php` ke **`htdocs/api.php`**.

### cPanel / public_html

```
public_html/
├── index.php
├── api.php              <-- upload file ini
└── ../katalog-umkm/     <-- aplikasi Laravel
```

Upload `api.php` ke **`public_html/api.php`**.

### Document root langsung ke folder public

Kalau hosting mengarahkan document root ke `katalog-umkm/public/`, upload ke:

```
public/api.php
```

File ini sudah ada di repo Laravel (`public/api.php`).

### Path otomatis

`api.php` mencari `vendor/autoload.php` di tiga kemungkinan:

1. `../vendor/autoload.php` (document root = `public/`)
2. `katalog-umkm/vendor/autoload.php` (app di dalam `htdocs/`)
3. `../katalog-umkm/vendor/autoload.php` (app di samping `public_html/`)

Kalau strukturmu berbeda, edit bagian `$autoloadCandidates` di baris atas file.

---

## 2. Tes

Buka:

```
https://zaaxcoo.gt.tc/api.php?action=ping
```

Harus muncul:

```json
{"ok":true,"time":"2026-10-29T05:35:25+00:00"}
```

Kalau muncul halaman 404 HTML, file belum ada di path yang benar.
Kalau muncul 500, cek `storage/logs/laravel.log`.

Tes data:

```
https://zaaxcoo.gt.tc/api.php?action=home
https://zaaxcoo.gt.tc/api.php?action=products&per_page=2
https://zaaxcoo.gt.tc/api.php?action=product&slug=kopi-arabica-200gr
```

---

## 3. Endpoint

### `GET ?action=ping`

Tes koneksi.

### `GET ?action=home`

Mengembalikan:

- `settings` — profil toko + `whatsapp_chat_url`
- `categories` — kategori aktif + jumlah produk + cover
- `promo_products` — promo yang terhubung ke produk tersedia
- `promo_announcements` — promo umum
- `featured` — maks 8 produk unggulan
- `latest` — maks 8 produk terbaru
- `sold_out` — maks 8 produk stok habis
- `best_sellers` — maks 4 produk terlaris
- `server_time`

### `GET ?action=products`

Query:

| Parameter | Default | Keterangan |
|---|---|---|
| `q` | kosong | Cari nama/deskripsi |
| `category` | kosong | Slug kategori |
| `sort` | `latest` | `latest`, `cheapest`, `priciest`, `name` |
| `page` | `1` | Halaman |
| `per_page` | `12` | Maks 24 |

Response:

```json
{
  "data": [ ...produk... ],
  "sold_out": [ ...produk stok habis (halaman 1)... ],
  "meta": {
    "page": 1,
    "per_page": 12,
    "total": 9,
    "last_page": 1,
    "has_more": false
  }
}
```

### `GET ?action=product&slug=...`

Mengembalikan `product`, `related`, `reviews`, dan `rating` (`average`, `count`).

### `GET ?action=categories`

Daftar kategori aktif.

### `GET ?action=settings`

Profil toko saja.

### `GET ?action=track&order_number=...&phone=...`

Mencari pesanan berdasarkan nomor pesanan + nomor WhatsApp. Normalisasi nomor sama dengan web (`08`, `628`, `+62` dianggap sama).

Response berisi:

- `order_number`, `status`, `status_label`
- `status_history` (dari → ke, waktu)
- `customer`
- `items` (snapshot nama, harga, qty, subtotal, gambar)
- `reviews`
- `can_review`
- `whatsapp_url`

### `POST ?action=order`

Body JSON:

```json
{
  "customer": {
    "name": "Nama Pembeli",
    "phone": "081234567890",
    "address": "Alamat lengkap",
    "note": "Catatan opsional"
  },
  "items": [
    { "product_id": 1, "quantity": 2 },
    { "product_id": 3, "quantity": 1 }
  ]
}
```

Yang dilakukan server:

1. Validasi input.
2. Ambil produk fresh dengan `lockForUpdate`.
3. Cek aktif + stok.
4. Hitung ulang harga dari database (termasuk promo).
5. Buat nomor pesanan `ORD-YYYYMMDD-XXXXXX`.
6. Buat `orders` + `order_items`.
7. Kurangi stok.
8. Generate pesan + URL WhatsApp.
9. Commit transaksi.

Response `201`:

```json
{
  "order": { ... },
  "whatsapp_url": "https://wa.me/62...?text=..."
}
```

Kalau stok berubah, response `422` dengan pesan yang jelas (mis. `Stok Kopi Arabica tidak mencukupi (tersisa 2).`).

### `POST ?action=review`

Body JSON:

```json
{
  "order_number": "ORD-20261029-ABC123",
  "phone": "081234567890",
  "item_id": 28,
  "rating": 5,
  "review": "Mantap, kopinya enak"
}
```

Syarat:

- Pesanan harus berstatus `completed`.
- Satu produk hanya bisa diulas sekali per pesanan.
- Nomor pesanan + WhatsApp harus cocok.

---

## 4. Keamanan & batasan

- **API publik** untuk data katalog, sama seperti website. Tidak ada data sensitif di endpoint katalog.
- **Lacak pesanan** butuh nomor pesanan + nomor WhatsApp yang dipakai saat checkout.
- **Ulasan** butuh nomor pesanan + nomor WhatsApp.
- **Rate limit** per IP:
  - Buat pesanan: 8 kali / 5 menit.
  - Lacak: 30 kali / menit.
  - Ulasan: 15 kali / menit.
- **Harga & stok** selalu dihitung ulang di server.
- **Tidak ada payment gateway**; pembayaran/konfirmasi lewat WhatsApp seperti web.
- **CORS** dibuka (`*`) supaya bisa dites dari tools; app native tidak membutuhkannya.

Kalau ingin menambah API key:

1. Set `MOBILE_API_KEY=...` di `.env` hosting.
2. Tambahkan pengecekan `$_SERVER['HTTP_X_API_KEY']` di `api.php`.
3. Kirim header `X-Api-Key` dari app.

Saat ini belum diaktifkan supaya setup tetap satu file.

---

## 5. Troubleshooting

| Gejala | Kemungkinan | Solusi |
|---|---|---|
| 404 HTML | File belum di-upload / path salah | Upload ke `htdocs/api.php`, tes `?action=ping` |
| 500 | Error PHP/Laravel | Cek `storage/logs/laravel.log`, pastikan `vendor/` lengkap, PHP 8.3 |
| 500 "Aplikasi Laravel tidak ditemukan" | Path autoload tidak cocok | Edit `$autoloadCandidates` |
| 403 / halaman `aes.js` | Tantangan InfinityFree | Normal di browser; app menyelesaikannya otomatis |
| `Terlalu banyak permintaan` | Rate limit | Tunggu 1–5 menit |
| Gambar 404 | File gambar tidak ada / path salah | Cek `storage/app/public/products/...` dan route `/storage/...` |
| Order 422 | Stok berubah / produk nonaktif | Muat ulang katalog, sesuaikan jumlah |
| Cache rate limit aneh | Cache lama | `php artisan optimize:clear` |

---

## 6. Update di kemudian hari

Kalau logic checkout web berubah (mis. diskon, ongkir, status), samakan juga di `api.php` supaya app dan web tetap konsisten.

File ini sengaja berupa satu file supaya upload ke shared hosting tidak perlu menyentuh `routes/` atau `bootstrap/app.php`.
