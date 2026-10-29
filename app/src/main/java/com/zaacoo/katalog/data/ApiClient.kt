@file:OptIn(ExperimentalSerializationApi::class)

package com.zaacoo.katalog.data

import com.zaacoo.katalog.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.TimeUnit

class ApiClient(prefs: Prefs) {

    private val json = Json {
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
        explicitNulls = false
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(ChallengeInterceptor(prefs))
        .build()

    private val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/')

    suspend fun home(): HomeResponse = get("home")

    suspend fun products(
        query: String,
        category: String,
        sort: String,
        page: Int,
        perPage: Int = 12,
    ): ProductListResponse = get(
        "products",
        buildMap {
            if (query.isNotBlank()) put("q", query.trim())
            if (category.isNotBlank()) put("category", category)
            if (sort.isNotBlank()) put("sort", sort)
            put("page", page.toString())
            put("per_page", perPage.toString())
        },
    )

    suspend fun product(slug: String): ProductDetailResponse = get("product", mapOf("slug" to slug))

    suspend fun categories(): CategoryListResponse = get("categories")

    suspend fun track(orderNumber: String, phone: String): TrackResponse = get(
        "track",
        mapOf("order_number" to orderNumber.trim(), "phone" to phone.trim()),
    )

    suspend fun createOrder(request: CreateOrderRequest): OrderResponse =
        post("order", json.encodeToString(CreateOrderRequest.serializer(), request))

    suspend fun createReview(request: CreateReviewRequest): ReviewResponse =
        post("review", json.encodeToString(CreateReviewRequest.serializer(), request))

    private suspend inline fun <reified T> get(
        action: String,
        params: Map<String, String> = emptyMap(),
    ): T = withContext(Dispatchers.IO) {
        val url = buildUrl(action, params)
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .build()

        execute(request)
    }

    private suspend inline fun <reified T> post(
        action: String,
        payload: String,
    ): T = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(buildUrl(action))
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .build()

        execute(request)
    }

    private inline fun <reified T> execute(request: Request): T {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            val contentType = response.header("Content-Type").orEmpty()

            if (contentType.contains("text/html", ignoreCase = true)) {
                val message = if (response.code == 404) {
                    "API mobile belum aktif di server. Upload backend/api.php ke hosting, lalu coba lagi."
                } else {
                    "Server mengirim halaman web, bukan data aplikasi. Pastikan api.php sudah di-upload dan berjalan."
                }
                throw ApiException(message, response.code)
            }

            if (!response.isSuccessful) {
                val message = body
                    ?.let { runCatching { json.decodeFromString<ApiErrorEnvelope>(it).error.message }.getOrNull() }
                    ?: defaultMessage(response.code)
                throw ApiException(message, response.code)
            }

            if (body.isNullOrBlank()) {
                throw ApiException("Server mengirim respons kosong.", response.code)
            }

            return runCatching { json.decodeFromString<T>(body) }
                .getOrElse { throw ApiException("Format data dari server tidak dikenali.", response.code) }
        }
    }

    private fun buildUrl(action: String, params: Map<String, String> = emptyMap()): String {
        val builder = baseUrl.toHttpUrlOrNull()?.newBuilder()
            ?: throw ApiException("Alamat server tidak valid: $baseUrl")

        builder.addPathSegment("api.php")
        builder.addQueryParameter("action", action)
        params.forEach { (key, value) -> builder.addQueryParameter(key, value) }

        return builder.build().toString()
    }

    private fun defaultMessage(code: Int): String = when (code) {
        400 -> "Permintaan tidak valid."
        404 -> "Data tidak ditemukan."
        422 -> "Data yang dikirim belum lengkap."
        429 -> "Terlalu banyak permintaan. Coba lagi sebentar."
        in 500..599 -> "Server sedang bermasalah. Coba lagi sebentar."
        else -> "Permintaan gagal (kode $code)."
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val USER_AGENT = "ZaaCo-Android/1.0"
    }
}
