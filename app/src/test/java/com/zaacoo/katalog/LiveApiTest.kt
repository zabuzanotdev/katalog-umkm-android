@file:OptIn(ExperimentalSerializationApi::class)

package com.zaacoo.katalog

import com.zaacoo.katalog.data.ChallengeSolver
import com.zaacoo.katalog.data.HomeResponse
import com.zaacoo.katalog.data.ProductListResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

/**
 * Tes manual ke API live. Jalankan hanya saat ingin memastikan hosting
 * benar-benar mengembalikan JSON yang cocok dengan model app.
 */
@Ignore("Manual live API test; butuh koneksi ke hosting")
class LiveApiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
        explicitNulls = false
        coerceInputValues = true
    }

    private val client = OkHttpClient()

    private fun get(url: String): String {
        val firstRequest = Request.Builder()
            .url(url)
            .header("User-Agent", "ZaaCo-Android/1.0")
            .build()

        var response = client.newCall(firstRequest).execute()
        var body = response.body?.string().orEmpty()

        if (body.contains("aes.js") && body.contains("slowAES")) {
            val cookie = ChallengeSolver.solve(body) ?: error("Gagal solve challenge")
            response.close()

            val retry = firstRequest.newBuilder()
                .header("Cookie", "__test=$cookie")
                .build()

            response = client.newCall(retry).execute()
            body = response.body?.string().orEmpty()
        }

        response.close()
        return body
    }

    @Test
    fun liveHome() {
        val body = get("https://zaaxcoo.gt.tc/api.php?action=home")
        val home = json.decodeFromString<HomeResponse>(body)

        println(
            "LIVE HOME: ${home.settings.businessName} | latest=${home.latest.size} | " +
                "categories=${home.categories.size} | featured=${home.featured.size} | " +
                "promo=${home.promoProducts.size}",
        )

        assertTrue(home.settings.businessName.isNotBlank())
        assertTrue(home.latest.isNotEmpty())
        assertTrue(home.categories.isNotEmpty())
    }

    @Test
    fun liveProducts() {
        val body = get("https://zaaxcoo.gt.tc/api.php?action=products&per_page=3")
        val products = json.decodeFromString<ProductListResponse>(body)

        println(
            "LIVE PRODUCTS: total=${products.meta.total} | first=${products.data.firstOrNull()?.name} | " +
                "image=${products.data.firstOrNull()?.imageUrl}",
        )

        assertTrue(products.meta.total > 0)
        assertTrue(products.data.isNotEmpty())
    }
}
