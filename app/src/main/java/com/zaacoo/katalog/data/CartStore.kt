package com.zaacoo.katalog.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class CartStore(private val prefs: Prefs) {

    private val json = Json { ignoreUnknownKeys = true }

    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun add(product: Product, quantity: Int = 1) {
        if (product.stock <= 0) return

        val current = _items.value.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        val existingQuantity = if (index >= 0) current[index].quantity else 0
        val newQuantity = (existingQuantity + quantity).coerceIn(1, product.stock)

        val item = CartItem(
            productId = product.id,
            name = product.name,
            slug = product.slug,
            price = product.price,
            promoPrice = product.promoPrice,
            imageUrl = product.imageUrl,
            stock = product.stock,
            quantity = newQuantity,
        )

        if (index >= 0) current[index] = item else current.add(item)
        update(current)
    }

    fun setQuantity(productId: Int, quantity: Int) {
        val current = _items.value.toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index < 0) return

        if (quantity <= 0) {
            current.removeAt(index)
        } else {
            val item = current[index]
            current[index] = item.copy(quantity = quantity.coerceAtMost(item.stock.coerceAtLeast(1)))
        }

        update(current)
    }

    fun remove(productId: Int) {
        update(_items.value.filterNot { it.productId == productId })
    }

    fun clear() {
        update(emptyList())
    }

    fun total(items: List<CartItem> = _items.value): Int = items.sumOf { it.subtotal }

    private fun update(items: List<CartItem>) {
        _items.value = items
        prefs.cartJson = json.encodeToString(ListSerializer(CartItem.serializer()), items)
    }

    private fun load(): List<CartItem> {
        val raw = prefs.cartJson ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(CartItem.serializer()), raw)
        }.getOrDefault(emptyList())
    }
}
