package com.zaacoo.katalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaacoo.katalog.data.KatalogRepository
import com.zaacoo.katalog.data.ProductDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    data object Loading : ProductUiState
    data class Content(val detail: ProductDetailResponse) : ProductUiState
    data class Error(val message: String) : ProductUiState
}

class ProductViewModel(
    private val repository: KatalogRepository,
    private val slug: String,
) : ViewModel() {

    private val _state = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val state: StateFlow<ProductUiState> = _state.asStateFlow()

    private var requestSeq = 0

    init {
        load()
    }

    fun load() {
        val requestId = ++requestSeq
        _state.value = ProductUiState.Loading

        viewModelScope.launch {
            val result = runCatching { repository.product(slug) }
            if (requestId != requestSeq) return@launch

            result
                .onSuccess { _state.value = ProductUiState.Content(it) }
                .onFailure {
                    _state.value = ProductUiState.Error(it.message ?: "Gagal memuat produk.")
                }
        }
    }
}
