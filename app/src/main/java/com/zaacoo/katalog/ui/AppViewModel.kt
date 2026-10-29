package com.zaacoo.katalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaacoo.katalog.data.CartItem
import com.zaacoo.katalog.data.CartStore
import com.zaacoo.katalog.data.HomeResponse
import com.zaacoo.katalog.data.KatalogRepository
import com.zaacoo.katalog.data.Product
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(
        val home: HomeResponse,
        val fromCache: Boolean = false,
        val refreshing: Boolean = false,
        val message: String? = null,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class AppViewModel(
    private val repository: KatalogRepository,
    private val cart: CartStore,
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    val cartItems: StateFlow<List<CartItem>> = cart.items

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (loadJob?.isActive == true) return

        val cached = repository.cachedHome()
        val current = _homeState.value

        if (cached != null) {
            _homeState.value = HomeUiState.Content(
                home = cached,
                fromCache = true,
                refreshing = true,
            )
        } else if (current !is HomeUiState.Content) {
            _homeState.value = HomeUiState.Loading
        } else {
            _homeState.value = current.copy(refreshing = true, message = null)
        }

        loadJob = viewModelScope.launch {
            runCatching { repository.home() }
                .onSuccess { home ->
                    repository.cacheHome(home)
                    _homeState.value = HomeUiState.Content(home)
                }
                .onFailure { error ->
                    val state = _homeState.value
                    _homeState.value = if (state is HomeUiState.Content) {
                        state.copy(
                            fromCache = true,
                            refreshing = false,
                            message = error.message ?: "Gagal memperbarui data.",
                        )
                    } else {
                        HomeUiState.Error(error.message ?: "Tidak bisa terhubung ke server.")
                    }
                }
        }
    }

    fun refresh() = load()

    fun addToCart(product: Product, quantity: Int) = cart.add(product, quantity)

    fun setQuantity(productId: Int, quantity: Int) = cart.setQuantity(productId, quantity)

    fun removeFromCart(productId: Int) = cart.remove(productId)

    fun clearCart() = cart.clear()
}
