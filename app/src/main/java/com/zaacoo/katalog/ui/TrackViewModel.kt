package com.zaacoo.katalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaacoo.katalog.data.KatalogRepository
import com.zaacoo.katalog.data.Order
import com.zaacoo.katalog.data.OrderItem
import com.zaacoo.katalog.data.Prefs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackUiState(
    val orderNumber: String = "",
    val phone: String = "",
    val loading: Boolean = false,
    val order: Order? = null,
    val error: String? = null,
    val reviewItem: OrderItem? = null,
    val reviewSubmitting: Boolean = false,
    val reviewError: String? = null,
)

class TrackViewModel(
    private val repository: KatalogRepository,
    private val prefs: Prefs,
) : ViewModel() {

    private val _state = MutableStateFlow(
        TrackUiState(
            orderNumber = prefs.lastOrderNumber,
            phone = prefs.lastOrderPhone,
        ),
    )
    val state: StateFlow<TrackUiState> = _state.asStateFlow()

    private var lookupJob: Job? = null
    private var lookupSeq = 0

    init {
        // Tampilkan pesanan terakhir begitu tab Pesanan dibuka, tanpa perlu
        // mengetik ulang nomor pesanan.
        if (prefs.lastOrderNumber.isNotBlank() && prefs.lastOrderPhone.isNotBlank()) {
            lookup()
        }
    }

    fun setOrderNumber(value: String) {
        _state.update { it.copy(orderNumber = value) }
    }

    fun setPhone(value: String) {
        _state.update { it.copy(phone = value) }
    }

    fun lookup() {
        val current = _state.value

        if (current.orderNumber.isBlank() || current.phone.isBlank()) {
            _state.update { it.copy(error = "Isi nomor pesanan dan nomor WhatsApp.") }
            return
        }

        lookupJob?.cancel()
        val requestId = ++lookupSeq
        _state.update { it.copy(loading = true, error = null, order = null) }

        lookupJob = viewModelScope.launch {
            val result = runCatching { repository.track(current.orderNumber, current.phone) }

            if (requestId != lookupSeq) return@launch

            result
                .onSuccess { order ->
                    prefs.lastOrderNumber = current.orderNumber.trim()
                    prefs.lastOrderPhone = current.phone.trim()
                    _state.update { it.copy(loading = false, order = order, error = null) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "Pesanan tidak ditemukan.",
                        )
                    }
                }
        }
    }

    fun startReview(item: OrderItem) {
        _state.update { it.copy(reviewItem = item, reviewError = null) }
    }

    fun dismissReview() {
        if (!_state.value.reviewSubmitting) {
            _state.update { it.copy(reviewItem = null, reviewError = null) }
        }
    }

    fun submitReview(rating: Int, text: String) {
        val current = _state.value
        val order = current.order ?: return
        val item = current.reviewItem ?: return

        _state.update { it.copy(reviewSubmitting = true, reviewError = null) }

        viewModelScope.launch {
            runCatching {
                repository.createReview(
                    orderNumber = order.orderNumber,
                    phone = order.customer.phone,
                    itemId = item.id,
                    rating = rating,
                    review = text,
                )
            }
                .onSuccess {
                    _state.update {
                        it.copy(reviewSubmitting = false, reviewItem = null, reviewError = null)
                    }
                    refreshOrder(order.orderNumber, order.customer.phone)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            reviewSubmitting = false,
                            reviewError = error.message ?: "Gagal mengirim ulasan.",
                        )
                    }
                }
        }
    }

    private fun refreshOrder(orderNumber: String, phone: String) {
        viewModelScope.launch {
            runCatching { repository.track(orderNumber, phone) }
                .onSuccess { order -> _state.update { it.copy(order = order) } }
        }
    }
}
