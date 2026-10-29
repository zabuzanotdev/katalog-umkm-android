@file:OptIn(ExperimentalSerializationApi::class)

package com.zaacoo.katalog.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val businessName: String = "Katalog UMKM",
    val tagline: String? = null,
    val description: String? = null,
    val logoUrl: String? = null,
    val phone: String? = null,
    val whatsappNumber: String? = null,
    val email: String? = null,
    val address: String? = null,
    val openingHours: String? = null,
    val instagram: String? = null,
    val facebook: String? = null,
    val whatsappChatUrl: String? = null,
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String? = null,
    val productCount: Int = 0,
    val coverUrl: String? = null,
)

@Serializable
data class CategoryRef(
    val id: Int,
    val name: String,
    val slug: String,
)

@Serializable
data class CategoryListResponse(
    val data: List<Category> = emptyList(),
)

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String? = null,
    val price: Int,
    val promoPrice: Int? = null,
    val discountPercent: Int? = null,
    val stock: Int = 0,
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val imageUrl: String? = null,
    val soldQuantity: Int? = null,
    val category: CategoryRef? = null,
) {
    val unitPrice: Int get() = promoPrice ?: price
    val hasPromo: Boolean get() = promoPrice != null && promoPrice < price
}

@Serializable
data class Promotion(
    val id: Int,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val productId: Int? = null,
    val discountPercent: Int? = null,
    val endsAt: String? = null,
    val product: Product? = null,
)

@Serializable
data class HomeResponse(
    val settings: Settings,
    val categories: List<Category> = emptyList(),
    val promoProducts: List<Promotion> = emptyList(),
    val promoAnnouncements: List<Promotion> = emptyList(),
    val featured: List<Product> = emptyList(),
    val latest: List<Product> = emptyList(),
    val soldOut: List<Product> = emptyList(),
    val bestSellers: List<Product> = emptyList(),
    val serverTime: String? = null,
)

@Serializable
data class PageMeta(
    val page: Int = 1,
    val perPage: Int = 12,
    val total: Int = 0,
    val lastPage: Int = 1,
    val hasMore: Boolean = false,
)

@Serializable
data class ProductListResponse(
    val data: List<Product> = emptyList(),
    val soldOut: List<Product> = emptyList(),
    val meta: PageMeta = PageMeta(),
)

@Serializable
data class Rating(
    val average: Double? = null,
    val count: Int = 0,
)

@Serializable
data class Review(
    val id: Int,
    val productId: Int,
    val name: String,
    val rating: Int,
    val review: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class ProductDetailResponse(
    val product: Product,
    val related: List<Product> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val rating: Rating = Rating(),
)

@Serializable
data class StatusEntry(
    val from: String? = null,
    val fromLabel: String? = null,
    val to: String? = null,
    val toLabel: String? = null,
    val at: String? = null,
)

@Serializable
data class Customer(
    val name: String,
    val phone: String,
    val address: String,
    val note: String? = null,
)

@Serializable
data class OrderItem(
    val id: Int,
    val productId: Int? = null,
    val productName: String,
    val productSlug: String? = null,
    val price: Int,
    val quantity: Int,
    val subtotal: Int,
    val imageUrl: String? = null,
    val reviewed: Boolean = false,
)

@Serializable
data class Order(
    val orderNumber: String,
    val status: String,
    val statusLabel: String,
    val statusHistory: List<StatusEntry> = emptyList(),
    val customer: Customer,
    val totalAmount: Int,
    val createdAt: String? = null,
    val items: List<OrderItem> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val canReview: Boolean = false,
    val whatsappUrl: String? = null,
)

@Serializable
data class OrderResponse(
    val order: Order,
    val whatsappUrl: String? = null,
)

@Serializable
data class TrackResponse(
    val order: Order,
)

@Serializable
data class ReviewResponse(
    val review: Review,
)

@Serializable
data class CustomerInput(
    val name: String,
    val phone: String,
    val address: String,
    val note: String? = null,
)

@Serializable
data class OrderLineInput(
    val productId: Int,
    val quantity: Int,
)

@Serializable
data class CreateOrderRequest(
    val customer: CustomerInput,
    val items: List<OrderLineInput>,
)

@Serializable
data class CreateReviewRequest(
    val orderNumber: String,
    val phone: String,
    val itemId: Int,
    val rating: Int,
    val review: String? = null,
)

@Serializable
data class ApiErrorBody(
    val code: Int = 0,
    val message: String = "Terjadi kesalahan.",
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorBody = ApiErrorBody(),
)

@Serializable
data class CartItem(
    val productId: Int,
    val name: String,
    val slug: String,
    val price: Int,
    val promoPrice: Int? = null,
    val imageUrl: String? = null,
    val stock: Int = 0,
    val quantity: Int = 1,
) {
    val unitPrice: Int get() = promoPrice ?: price
    val subtotal: Int get() = unitPrice * quantity
    val hasPromo: Boolean get() = promoPrice != null && promoPrice < price
}

class ApiException(message: String, val code: Int = 0) : Exception(message)
