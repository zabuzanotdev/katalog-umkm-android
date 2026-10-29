@file:OptIn(ExperimentalSerializationApi::class)

package com.zaacoo.katalog.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

class KatalogRepository(
    private val api: ApiClient,
    private val prefs: Prefs,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
        explicitNulls = false
        coerceInputValues = true
    }

    fun cachedHome(): HomeResponse? {
        val raw = prefs.cachedHomeJson ?: return null
        return runCatching { json.decodeFromString<HomeResponse>(raw) }.getOrNull()
    }

    fun cacheHome(home: HomeResponse) {
        prefs.cachedHomeJson = json.encodeToString(HomeResponse.serializer(), home)
    }

    suspend fun home(): HomeResponse = api.home()

    suspend fun products(
        query: String,
        category: String,
        sort: String,
        page: Int,
    ): ProductListResponse = api.products(query, category, sort, page)

    suspend fun product(slug: String): ProductDetailResponse = api.product(slug)

    suspend fun categories(): List<Category> = api.categories().data

    suspend fun track(orderNumber: String, phone: String): Order = api.track(orderNumber, phone).order

    suspend fun createOrder(
        name: String,
        phone: String,
        address: String,
        note: String?,
        items: List<CartItem>,
    ): OrderResponse = api.createOrder(
        CreateOrderRequest(
            customer = CustomerInput(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                note = note?.trim()?.takeIf { it.isNotEmpty() },
            ),
            items = items.map { OrderLineInput(productId = it.productId, quantity = it.quantity) },
        ),
    )

    suspend fun createReview(
        orderNumber: String,
        phone: String,
        itemId: Int,
        rating: Int,
        review: String?,
    ): Review = api.createReview(
        CreateReviewRequest(
            orderNumber = orderNumber,
            phone = phone,
            itemId = itemId,
            rating = rating,
            review = review?.trim()?.takeIf { it.isNotEmpty() },
        ),
    ).review
}
