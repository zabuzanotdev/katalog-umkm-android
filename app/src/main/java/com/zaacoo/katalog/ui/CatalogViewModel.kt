package com.zaacoo.katalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaacoo.katalog.data.Category
import com.zaacoo.katalog.data.KatalogRepository
import com.zaacoo.katalog.data.Product
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val query: String = "",
    val category: String = "",
    val sort: String = "latest",
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val soldOut: List<Product> = emptyList(),
    val page: Int = 1,
    val total: Int = 0,
    val hasMore: Boolean = false,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
)

class CatalogViewModel(
    private val repository: KatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var requestSeq = 0

    init {
        loadCategories()
        load(reset = true)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            runCatching { repository.categories() }
                .onSuccess { categories ->
                    _state.update { it.copy(categories = categories) }
                }
        }
    }

    fun setQuery(query: String) {
        if (query == _state.value.query) return
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load(reset = true)
        }
    }

    fun setCategory(slug: String) {
        if (slug == _state.value.category) return
        _state.update { it.copy(category = slug) }
        load(reset = true)
    }

    fun setSort(sort: String) {
        if (sort == _state.value.sort) return
        _state.update { it.copy(sort = sort) }
        load(reset = true)
    }

    fun resetFilters() {
        searchJob?.cancel()
        _state.update { it.copy(query = "", category = "", sort = "latest") }
        load(reset = true)
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        load(reset = false)
    }

    fun retry() = load(reset = true)

    fun load(reset: Boolean) {
        if (!reset && loadJob?.isActive == true) return

        loadJob?.cancel()
        val requestId = ++requestSeq

        val snapshot = _state.value
        val page = if (reset) 1 else snapshot.page + 1

        _state.update {
            it.copy(
                loading = reset,
                loadingMore = !reset,
                error = null,
            )
        }

        loadJob = viewModelScope.launch {
            val result = runCatching {
                repository.products(
                    query = snapshot.query,
                    category = snapshot.category,
                    sort = snapshot.sort,
                    page = page,
                )
            }

            if (requestId != requestSeq) return@launch

            result
                .onSuccess { response ->
                    _state.update {
                        val merged = if (reset) {
                            response.data
                        } else {
                            (it.products + response.data).distinctBy { product -> product.id }
                        }

                        it.copy(
                            products = merged,
                            soldOut = if (reset) response.soldOut else it.soldOut,
                            page = response.meta.page,
                            total = response.meta.total,
                            hasMore = response.meta.hasMore,
                            loading = false,
                            loadingMore = false,
                            error = null,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            error = error.message ?: "Gagal memuat produk.",
                        )
                    }
                }
        }
    }
}
