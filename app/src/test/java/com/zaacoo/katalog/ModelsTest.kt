@file:OptIn(ExperimentalSerializationApi::class)

package com.zaacoo.katalog

import com.zaacoo.katalog.data.CreateOrderRequest
import com.zaacoo.katalog.data.CustomerInput
import com.zaacoo.katalog.data.HomeResponse
import com.zaacoo.katalog.data.OrderLineInput
import com.zaacoo.katalog.data.OrderResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun decodesHomeResponse() {
        val home = json.decodeFromString<HomeResponse>(HOME_JSON)

        assertEquals("Zaa & Coo", home.settings.businessName)
        assertEquals("6281234567890", home.settings.whatsappNumber)
        assertEquals(1, home.categories.size)
        assertEquals(2, home.categories.first().productCount)
        assertEquals(1, home.latest.size)
        assertEquals(45000, home.latest.first().price)
        assertEquals(40000, home.latest.first().promoPrice)
        assertEquals(11, home.latest.first().discountPercent)
        assertEquals("Minuman", home.latest.first().category?.name)
    }

    @Test
    fun decodesOrderResponse() {
        val response = json.decodeFromString<OrderResponse>(ORDER_JSON)

        assertEquals("ORD-20261029-ABC123", response.order.orderNumber)
        assertEquals("pending", response.order.status)
        assertEquals(90000, response.order.totalAmount)
        assertEquals(1, response.order.items.size)
        assertEquals(45000, response.order.items.first().price)
        assertEquals(2, response.order.items.first().quantity)
        assertTrue(response.whatsappUrl?.startsWith("https://wa.me/") == true)
    }

    @Test
    fun encodesOrderRequestWithSnakeCase() {
        val payload = json.encodeToString(
            CreateOrderRequest.serializer(),
            CreateOrderRequest(
                customer = CustomerInput(
                    name = "Tes Android",
                    phone = "081234567890",
                    address = "Jl. Tes No. 1",
                    note = null,
                ),
                items = listOf(OrderLineInput(productId = 1, quantity = 2)),
            ),
        )

        assertTrue(payload.contains("\"customer\""))
        assertTrue(payload.contains("\"name\":\"Tes Android\""))
        assertTrue(payload.contains("\"product_id\":1"))
        assertTrue(payload.contains("\"quantity\":2"))
        assertFalse(payload.contains("\"note\""))
    }

    private companion object {
        const val HOME_JSON = """
            {
              "settings": {
                "business_name": "Zaa & Coo",
                "tagline": "Rasa Lokal",
                "description": "UMKM kuliner lokal.",
                "logo_url": "https://zaaxcoo.gt.tc/storage/logos/logo.jpg",
                "phone": "081234567890",
                "whatsapp_number": "6281234567890",
                "email": "halo@example.id",
                "address": "Jl. Merdeka No. 10",
                "opening_hours": "Senin - Sabtu, 08.00 - 21.00",
                "instagram": "@zaacoo",
                "facebook": "Zaa & Coo",
                "whatsapp_chat_url": "https://wa.me/6281234567890?text=Halo"
              },
              "categories": [
                {"id": 2, "name": "Minuman", "slug": "minuman", "description": "Minuman segar", "product_count": 2, "cover_url": null}
              ],
              "promo_products": [],
              "promo_announcements": [],
              "featured": [],
              "latest": [
                {
                  "id": 1,
                  "name": "Kopi Arabica 200gr",
                  "slug": "kopi-arabica-200gr",
                  "description": "Kopi arabika pilihan.",
                  "price": 45000,
                  "promo_price": 40000,
                  "discount_percent": 11,
                  "stock": 20,
                  "is_available": true,
                  "is_featured": true,
                  "image_url": "https://zaaxcoo.gt.tc/storage/products/kopi.jpg",
                  "sold_quantity": null,
                  "category": {"id": 2, "name": "Minuman", "slug": "minuman"}
                }
              ],
              "sold_out": [],
              "best_sellers": [],
              "server_time": "2026-10-29T03:50:03+00:00"
            }
        """

        const val ORDER_JSON = """
            {
              "order": {
                "order_number": "ORD-20261029-ABC123",
                "status": "pending",
                "status_label": "Menunggu",
                "status_history": [],
                "customer": {
                  "name": "Tes Android",
                  "phone": "081234567890",
                  "address": "Jl. Tes No. 1",
                  "note": "Dibungkus rapi"
                },
                "total_amount": 90000,
                "created_at": "2026-10-29T03:50:03+00:00",
                "items": [
                  {
                    "id": 28,
                    "product_id": 1,
                    "product_name": "Kopi Arabica 200gr",
                    "product_slug": "kopi-arabica-200gr",
                    "price": 45000,
                    "quantity": 2,
                    "subtotal": 90000,
                    "image_url": null,
                    "reviewed": false
                  }
                ],
                "reviews": [],
                "can_review": false,
                "whatsapp_url": "https://wa.me/6281234567890?text=Pesanan"
              },
              "whatsapp_url": "https://wa.me/6281234567890?text=Pesanan"
            }
        """
    }
}
