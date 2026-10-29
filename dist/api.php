<?php

/**
 * Mobile API bridge untuk aplikasi Android Katalog UMKM.
 *
 * Cara pasang di hosting (InfinityFree / cPanel):
 * 1. Upload file ini ke folder public site, sejajar index.php.
 *    - InfinityFree : htdocs/api.php
 *    - cPanel        : public_html/api.php
 * 2. Tidak perlu migrate, route, atau composer. File ini memakai model &
 *    service Laravel yang sudah ada.
 * 3. Tes: https://domainkamu/api.php?action=ping
 *
 * Endpoint:
 *   GET  ?action=ping
 *   GET  ?action=home
 *   GET  ?action=products&q=&category=&page=1&per_page=12&sort=latest
 *   GET  ?action=product&slug=kopi-arabica-200gr
 *   GET  ?action=categories
 *   GET  ?action=settings
 *   GET  ?action=track&order_number=ORD-...&phone=08...
 *   POST ?action=order      body JSON {customer:{...}, items:[{product_id,quantity}]}
 *   POST ?action=review     body JSON {order_number, phone, item_id, rating, review}
 */

declare(strict_types=1);

use App\Models\BusinessSetting;
use App\Models\Category;
use App\Models\Order;
use App\Models\Product;
use App\Models\Promotion;
use App\Services\WhatsAppService;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

ini_set('display_errors', '0');
error_reporting(E_ALL);

define('LARAVEL_START', microtime(true));

/*
 * Cari lokasi aplikasi Laravel. Tiga kemungkinan struktur hosting:
 * 1. api.php di dalam folder public/ aplikasi (document root = public)
 * 2. api.php di htdocs/, aplikasi di htdocs/katalog-umkm/
 * 3. api.php di public_html/, aplikasi di ../katalog-umkm/
 */
$autoloadCandidates = [
    __DIR__.'/../vendor/autoload.php',
    __DIR__.'/katalog-umkm/vendor/autoload.php',
    __DIR__.'/../katalog-umkm/vendor/autoload.php',
];

$autoload = null;

foreach ($autoloadCandidates as $candidate) {
    if (is_file($candidate)) {
        $autoload = $candidate;
        break;
    }
}

if ($autoload === null) {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'error' => [
            'code' => 500,
            'message' => 'Aplikasi Laravel tidak ditemukan. Sesuaikan path autoload di api.php.',
        ],
    ]);
    exit;
}

require $autoload;

// autoload berada di <app-root>/vendor/autoload.php
$appRoot = dirname($autoload, 2);

/** @var \Illuminate\Foundation\Application $app */
$app = require_once $appRoot.'/bootstrap/app.php';
$app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap();

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Accept, X-Api-Key');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Cache-Control: no-store, max-age=0');
header('X-Content-Type-Options: nosniff');

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'OPTIONS') {
    http_response_code(204);
    exit;
}

final class ApiError extends RuntimeException
{
    public function __construct(public readonly int $status, string $message)
    {
        parent::__construct($message);
    }
}

function fail(int $status, string $message): never
{
    throw new ApiError($status, $message);
}

function respond(array $data, int $status = 200): never
{
    http_response_code($status);
    echo json_encode(
        $data,
        JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_INVALID_UTF8_SUBSTITUTE
    );
    exit;
}

function api_base(): string
{
    $forwarded = strtolower((string) ($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? ''));
    $https = (! empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') || $forwarded === 'https';
    $host = $_SERVER['HTTP_HOST'] ?? parse_url((string) config('app.url'), PHP_URL_HOST) ?: 'localhost';

    return ($https ? 'https' : 'http').'://'.$host;
}

function media_url(?string $path): ?string
{
    if ($path === null || trim($path) === '') {
        return null;
    }

    $segments = array_map('rawurlencode', explode('/', ltrim($path, '/')));

    return api_base().'/storage/'.implode('/', $segments);
}

function json_input(): array
{
    if (! empty($_POST)) {
        return $_POST;
    }

    $raw = file_get_contents('php://input');

    if ($raw === false || trim($raw) === '') {
        return [];
    }

    $data = json_decode($raw, true);

    if (! is_array($data)) {
        fail(400, 'Body JSON tidak valid.');
    }

    return $data;
}

function require_string(array $data, string $key, int $min, int $max, string $label): string
{
    $value = trim((string) ($data[$key] ?? ''));

    if (mb_strlen($value) < $min) {
        fail(422, $label.' minimal '.$min.' karakter.');
    }

    if (mb_strlen($value) > $max) {
        fail(422, $label.' maksimal '.$max.' karakter.');
    }

    return $value;
}

function optional_string(array $data, string $key, int $max, string $label): ?string
{
    $value = trim((string) ($data[$key] ?? ''));

    if ($value === '') {
        return null;
    }

    if (mb_strlen($value) > $max) {
        fail(422, $label.' maksimal '.$max.' karakter.');
    }

    return $value;
}

function product_query(): Builder
{
    return Product::query()->with([
        'category',
        'promotions' => fn ($query) => $query->ongoing(),
    ]);
}

function product_payload(Product $product): array
{
    $promoPrice = $product->promoPrice();

    return [
        'id' => (int) $product->id,
        'name' => $product->name,
        'slug' => $product->slug,
        'description' => $product->description,
        'price' => (int) $product->price,
        'promo_price' => $promoPrice,
        'discount_percent' => $product->activeDiscountPercent(),
        'stock' => (int) $product->stock,
        'is_available' => $product->isAvailable(),
        'is_featured' => (bool) $product->is_featured,
        'image_url' => media_url($product->image),
        'sold_quantity' => isset($product->sold_quantity) ? (int) $product->sold_quantity : null,
        'category' => $product->category ? [
            'id' => (int) $product->category->id,
            'name' => $product->category->name,
            'slug' => $product->category->slug,
        ] : null,
    ];
}

function settings_payload(?BusinessSetting $setting): array
{
    return [
        'business_name' => $setting?->business_name ?? 'Katalog UMKM',
        'tagline' => $setting?->tagline,
        'description' => $setting?->description,
        'logo_url' => media_url($setting?->logo),
        'phone' => $setting?->phone,
        'whatsapp_number' => $setting?->whatsapp_number,
        'email' => $setting?->email,
        'address' => $setting?->address,
        'opening_hours' => $setting?->opening_hours,
        'instagram' => $setting?->instagram,
        'facebook' => $setting?->facebook,
        'whatsapp_chat_url' => WhatsAppService::chatUrl('Halo, saya ingin bertanya tentang produk dan promo yang tersedia.'),
    ];
}

function categories_payload(): array
{
    $categories = Category::query()
        ->where('is_active', true)
        ->withCount(['products' => fn (Builder $query) => $query
            ->where('is_active', true)
            ->where('stock', '>', 0)])
        ->orderBy('name')
        ->get();

    $covers = Product::query()
        ->whereIn('category_id', $categories->pluck('id'))
        ->whereNotNull('image')
        ->latest()
        ->get(['id', 'category_id', 'image'])
        ->groupBy('category_id')
        ->map(fn ($products) => $products->first()->image);

    return $categories->map(fn (Category $category) => [
        'id' => (int) $category->id,
        'name' => $category->name,
        'slug' => $category->slug,
        'description' => $category->description,
        'product_count' => (int) $category->products_count,
        'cover_url' => media_url($covers->get($category->id)),
    ])->all();
}

function promotion_payload(Promotion $promotion): array
{
    return [
        'id' => (int) $promotion->id,
        'title' => $promotion->title,
        'description' => $promotion->description,
        'image_url' => media_url($promotion->image),
        'product_id' => $promotion->product_id ? (int) $promotion->product_id : null,
        'discount_percent' => $promotion->discount_percent ? (int) $promotion->discount_percent : null,
        'ends_at' => $promotion->ends_at?->toIso8601String(),
        'product' => $promotion->relationLoaded('product') && $promotion->product
            ? product_payload($promotion->product)
            : null,
    ];
}

function review_payload($review): array
{
    return [
        'id' => (int) $review->id,
        'product_id' => (int) $review->product_id,
        'name' => $review->name,
        'rating' => (int) $review->rating,
        'review' => $review->review,
        'created_at' => $review->created_at?->toIso8601String(),
    ];
}

function order_payload(Order $order): array
{
    $order->loadMissing(['items.product', 'reviews']);

    $reviewedProductIds = $order->reviews->pluck('product_id')->all();

    return [
        'order_number' => $order->order_number,
        'status' => $order->status,
        'status_label' => Order::statusLabel($order->status),
        'status_history' => collect($order->status_history ?? [])
            ->map(fn (array $entry) => [
                'from' => $entry['from'] ?? null,
                'from_label' => isset($entry['from']) ? Order::statusLabel($entry['from']) : null,
                'to' => $entry['to'] ?? null,
                'to_label' => isset($entry['to']) ? Order::statusLabel($entry['to']) : null,
                'at' => $entry['at'] ?? null,
            ])
            ->values()
            ->all(),
        'customer' => [
            'name' => $order->customer_name,
            'phone' => $order->customer_phone,
            'address' => $order->customer_address,
            'note' => $order->customer_note,
        ],
        'total_amount' => (int) $order->total_amount,
        'created_at' => $order->created_at?->toIso8601String(),
        'items' => $order->items->map(fn ($item) => [
            'id' => (int) $item->id,
            'product_id' => $item->product_id ? (int) $item->product_id : null,
            'product_name' => $item->product_name,
            'product_slug' => $item->product?->slug,
            'price' => (int) $item->price,
            'quantity' => (int) $item->quantity,
            'subtotal' => (int) $item->subtotal,
            'image_url' => media_url($item->product?->image),
            'reviewed' => in_array((int) $item->product_id, $reviewedProductIds, true),
        ])->values()->all(),
        'reviews' => $order->reviews->map(fn ($review) => review_payload($review))->values()->all(),
        'can_review' => $order->isCompleted(),
        'whatsapp_url' => $order->whatsapp_url,
    ];
}

function generate_order_number(): string
{
    do {
        $number = 'ORD-'.now()->format('Ymd').'-'.Str::upper(Str::random(6));
    } while (Order::where('order_number', $number)->exists());

    return $number;
}

function throttle_requests(string $bucket, int $max, int $seconds): void
{
    $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
    $key = 'mobile-api:'.$bucket.':'.sha1($ip);
    $count = (int) Cache::get($key, 0);

    if ($count >= $max) {
        fail(429, 'Terlalu banyak permintaan. Coba lagi sebentar.');
    }

    Cache::put($key, $count + 1, $seconds);
}

function handle_home(): never
{
    $setting = BusinessSetting::current();

    $featured = product_query()
        ->available()
        ->where('is_featured', true)
        ->latest()
        ->take(8)
        ->get();

    $latest = product_query()
        ->available()
        ->latest()
        ->take(8)
        ->get();

    $soldOut = product_query()
        ->soldOut()
        ->latest()
        ->take(8)
        ->get();

    $bestSellers = Product::bestSellers(4);

    $promoProducts = Promotion::ongoing()
        ->whereHas('product', fn (Builder $query) => $query->available())
        ->with([
            'product.category',
            'product.promotions' => fn ($query) => $query->ongoing(),
        ])
        ->latest()
        ->take(4)
        ->get();

    $promoAnnouncements = Promotion::ongoing()
        ->whereDoesntHave('product', fn (Builder $query) => $query->available())
        ->latest()
        ->take(5)
        ->get();

    respond([
        'settings' => settings_payload($setting),
        'categories' => categories_payload(),
        'promo_products' => $promoProducts->map(fn (Promotion $promotion) => promotion_payload($promotion))->all(),
        'promo_announcements' => $promoAnnouncements->map(fn (Promotion $promotion) => promotion_payload($promotion))->all(),
        'featured' => $featured->map(fn (Product $product) => product_payload($product))->all(),
        'latest' => $latest->map(fn (Product $product) => product_payload($product))->all(),
        'sold_out' => $soldOut->map(fn (Product $product) => product_payload($product))->all(),
        'best_sellers' => $bestSellers->map(fn (Product $product) => product_payload($product))->all(),
        'server_time' => now()->toIso8601String(),
    ]);
}

function handle_products(): never
{
    $search = trim((string) ($_GET['q'] ?? ''));
    $category = trim((string) ($_GET['category'] ?? ''));
    $sort = (string) ($_GET['sort'] ?? 'latest');
    $page = max(1, (int) ($_GET['page'] ?? 1));
    $perPage = min(24, max(1, (int) ($_GET['per_page'] ?? 12)));

    $applyFilters = function (Builder $query) use ($search, $category): Builder {
        return $query
            ->when($search !== '', function (Builder $builder) use ($search) {
                $builder->where(function (Builder $inner) use ($search) {
                    $inner->where('name', 'like', '%'.$search.'%')
                        ->orWhere('description', 'like', '%'.$search.'%');
                });
            })
            ->when($category !== '', function (Builder $builder) use ($category) {
                $builder->whereHas('category', fn (Builder $inner) => $inner->where('slug', $category));
            });
    };

    $base = $applyFilters(product_query()->available());

    $base = match ($sort) {
        'cheapest' => $base->orderBy('price'),
        'priciest' => $base->orderByDesc('price'),
        'name' => $base->orderBy('name'),
        default => $base->latest(),
    };

    $total = (clone $base)->count();
    $lastPage = max(1, (int) ceil($total / $perPage));
    $items = $base->forPage($page, $perPage)->get();

    $soldOut = collect();

    if ($page === 1) {
        $soldOut = $applyFilters(product_query()->soldOut())
            ->latest()
            ->take(8)
            ->get();
    }

    respond([
        'data' => $items->map(fn (Product $product) => product_payload($product))->values()->all(),
        'sold_out' => $soldOut->map(fn (Product $product) => product_payload($product))->values()->all(),
        'meta' => [
            'page' => $page,
            'per_page' => $perPage,
            'total' => $total,
            'last_page' => $lastPage,
            'has_more' => $page < $lastPage,
        ],
    ]);
}

function handle_product(): never
{
    $slug = trim((string) ($_GET['slug'] ?? ''));

    if ($slug === '') {
        fail(422, 'Slug produk wajib diisi.');
    }

    $product = product_query()
        ->where('slug', $slug)
        ->where('is_active', true)
        ->first();

    if (! $product) {
        fail(404, 'Produk tidak ditemukan.');
    }

    $related = product_query()
        ->where('is_active', true)
        ->where('category_id', $product->category_id)
        ->where('id', '!=', $product->id)
        ->take(4)
        ->get();

    $reviews = $product->reviews()
        ->visible()
        ->latest()
        ->take(10)
        ->get();

    $rating = $product->reviews()->visible()->avg('rating');
    $ratingCount = $product->reviews()->visible()->count();

    respond([
        'product' => product_payload($product),
        'related' => $related->map(fn (Product $item) => product_payload($item))->values()->all(),
        'reviews' => $reviews->map(fn ($review) => review_payload($review))->values()->all(),
        'rating' => [
            'average' => $rating === null ? null : round((float) $rating, 1),
            'count' => (int) $ratingCount,
        ],
    ]);
}

function handle_categories(): never
{
    respond(['data' => categories_payload()]);
}

function handle_settings(): never
{
    respond(['settings' => settings_payload(BusinessSetting::current())]);
}

function handle_track(): never
{
    throttle_requests('track', 30, 60);

    $orderNumber = trim((string) ($_GET['order_number'] ?? ''));
    $phone = trim((string) ($_GET['phone'] ?? ''));

    if ($orderNumber === '' || $phone === '') {
        fail(422, 'Nomor pesanan dan nomor WhatsApp wajib diisi.');
    }

    $order = Order::findForCustomer($orderNumber, $phone);

    if (! $order) {
        fail(404, 'Pesanan tidak ditemukan. Periksa lagi nomor pesanan dan nomor WhatsApp yang kamu pakai saat checkout.');
    }

    respond(['order' => order_payload($order)]);
}

function handle_order(): never
{
    throttle_requests('order', 8, 300);

    $input = json_input();
    $customer = is_array($input['customer'] ?? null) ? $input['customer'] : [];
    $items = is_array($input['items'] ?? null) ? $input['items'] : [];

    $name = require_string($customer, 'name', 2, 255, 'Nama');
    $phone = require_string($customer, 'phone', 6, 30, 'Nomor WhatsApp');
    $address = require_string($customer, 'address', 5, 500, 'Alamat');
    $note = optional_string($customer, 'note', 500, 'Catatan');

    if (preg_replace('/[^0-9]/', '', $phone) === '' || strlen(preg_replace('/[^0-9]/', '', $phone)) < 8) {
        fail(422, 'Nomor WhatsApp tidak valid.');
    }

    if ($items === [] || count($items) > 50) {
        fail(422, 'Keranjang tidak valid.');
    }

    $quantities = [];

    foreach ($items as $item) {
        if (! is_array($item)) {
            fail(422, 'Item keranjang tidak valid.');
        }

        $productId = (int) ($item['product_id'] ?? 0);
        $quantity = (int) ($item['quantity'] ?? 0);

        if ($productId <= 0) {
            fail(422, 'Produk pada keranjang tidak valid.');
        }

        if ($quantity < 1 || $quantity > 999) {
            fail(422, 'Jumlah produk harus antara 1 dan 999.');
        }

        $quantities[$productId] = ($quantities[$productId] ?? 0) + $quantity;
    }

    $order = DB::transaction(function () use ($quantities, $name, $phone, $address, $note) {
        $fresh = Product::query()
            ->whereIn('id', array_keys($quantities))
            ->with(['promotions' => fn ($query) => $query->ongoing()])
            ->lockForUpdate()
            ->get()
            ->keyBy('id');

        $total = 0;
        $lines = [];
        $hasPromo = false;

        foreach ($quantities as $productId => $quantity) {
            $product = $fresh->get($productId);

            if (! $product || ! $product->is_active) {
                fail(422, 'Ada produk yang sudah tidak tersedia. Muat ulang katalog.');
            }

            if ($product->stock <= 0) {
                fail(422, 'Stok '.$product->name.' sudah habis.');
            }

            if ($quantity > $product->stock) {
                fail(422, 'Stok '.$product->name.' tidak mencukupi (tersisa '.$product->stock.').');
            }

            $unitPrice = $product->promoPrice() ?? $product->price;
            $subtotal = $unitPrice * $quantity;
            $total += $subtotal;

            if ($unitPrice < $product->price) {
                $hasPromo = true;
            }

            $lines[] = [
                'product_id' => $product->id,
                'product_name' => $product->name,
                'price' => $unitPrice,
                'quantity' => $quantity,
                'subtotal' => $subtotal,
            ];
        }

        $orderNumber = generate_order_number();

        $message = WhatsAppService::buildMessage(
            collect($lines),
            [
                'name' => $name,
                'phone' => $phone,
                'address' => $address,
                'note' => $note,
            ],
            $total,
            $hasPromo,
            $orderNumber
        );

        $order = Order::create([
            'order_number' => $orderNumber,
            'customer_name' => $name,
            'customer_phone' => $phone,
            'customer_address' => $address,
            'customer_note' => $note,
            'total_amount' => $total,
            'status' => Order::STATUS_PENDING,
            'whatsapp_url' => WhatsAppService::buildUrl($message),
        ]);

        foreach ($lines as $line) {
            $order->items()->create($line);
        }

        foreach ($quantities as $productId => $quantity) {
            $fresh->get($productId)->decrement('stock', $quantity);
        }

        return $order;
    });

    respond([
        'order' => order_payload($order),
        'whatsapp_url' => $order->whatsapp_url,
    ], 201);
}

function handle_review(): never
{
    throttle_requests('review', 15, 60);

    $input = json_input();

    $orderNumber = trim((string) ($input['order_number'] ?? ''));
    $phone = trim((string) ($input['phone'] ?? ''));
    $itemId = (int) ($input['item_id'] ?? 0);
    $rating = (int) ($input['rating'] ?? 0);
    $reviewText = optional_string($input, 'review', 1000, 'Ulasan');

    if ($orderNumber === '' || $phone === '') {
        fail(422, 'Nomor pesanan dan nomor WhatsApp wajib diisi.');
    }

    if ($itemId <= 0) {
        fail(422, 'Item pesanan tidak valid.');
    }

    if ($rating < 1 || $rating > 5) {
        fail(422, 'Rating harus antara 1 dan 5.');
    }

    $order = Order::findForCustomer($orderNumber, $phone);

    if (! $order) {
        fail(404, 'Pesanan tidak ditemukan.');
    }

    if (! $order->isCompleted()) {
        fail(422, 'Ulasan hanya bisa dikirim setelah pesanan berstatus selesai.');
    }

    $item = $order->items()->whereKey($itemId)->first();

    if (! $item) {
        fail(404, 'Item pesanan tidak ditemukan.');
    }

    if ($item->product_id === null) {
        fail(422, 'Produk ini sudah tidak tersedia, ulasan tidak bisa dikirim.');
    }

    $review = $order->reviews()->firstOrCreate(
        ['product_id' => $item->product_id],
        [
            'name' => $order->customer_name,
            'rating' => $rating,
            'review' => $reviewText,
        ]
    );

    if (! $review->wasRecentlyCreated) {
        fail(409, 'Kamu sudah memberi ulasan untuk produk ini di pesanan ini.');
    }

    respond(['review' => review_payload($review)], 201);
}

try {
    $action = strtolower(trim((string) ($_GET['action'] ?? 'home')));
    $method = strtoupper((string) ($_SERVER['REQUEST_METHOD'] ?? 'GET'));

    $getActions = [
        'ping' => fn () => respond(['ok' => true, 'time' => now()->toIso8601String()]),
        'home' => fn () => handle_home(),
        'bootstrap' => fn () => handle_home(),
        'products' => fn () => handle_products(),
        'product' => fn () => handle_product(),
        'categories' => fn () => handle_categories(),
        'settings' => fn () => handle_settings(),
        'track' => fn () => handle_track(),
    ];

    $postActions = [
        'order' => fn () => handle_order(),
        'review' => fn () => handle_review(),
    ];

    if ($method === 'GET' && isset($getActions[$action])) {
        $getActions[$action]();
    }

    if ($method === 'POST' && isset($postActions[$action])) {
        $postActions[$action]();
    }

    fail(404, 'Endpoint tidak ditemukan.');
} catch (ApiError $error) {
    respond([
        'error' => [
            'code' => $error->status,
            'message' => $error->getMessage(),
        ],
    ], $error->status);
} catch (Throwable $error) {
    \Illuminate\Support\Facades\Log::error('Mobile API error: '.$error->getMessage(), [
        'exception' => $error,
    ]);

    $debug = (bool) config('app.debug');

    respond([
        'error' => [
            'code' => 500,
            'message' => $debug
                ? $error->getMessage()
                : 'Terjadi kesalahan di server. Coba lagi sebentar.',
        ],
    ], 500);
}
