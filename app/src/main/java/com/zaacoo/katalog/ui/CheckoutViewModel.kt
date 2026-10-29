package com.zaacoo.katalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaacoo.katalog.data.CartStore
import com.zaacoo.katalog.data.KatalogRepository
import com.zaacoo.katalog.data.Order
import com.zaacoo.katalog.data.Prefs
import com.zaacoo.katalog.util.isValidPhone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CheckoutUiState {
    data object Idle : CheckoutUiState
    data object Submitting : CheckoutUiState
    data class Success(val order: Order, val whatsappUrl: String?) : CheckoutUiState
    data class Error(val message: String) : CheckoutUiState
}

class CheckoutViewModel(
    private val repository: KatalogRepository,
    private val cart: CartStore,
    private val prefs: Prefs,
) : ViewModel() {

    private val _state = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val state: StateFlow<CheckoutUiState> = _state.asStateFlow()

    val initialName: String get() = prefs.customerName
    val initialPhone: String get() = prefs.customerPhone
    val initialAddress: String get() = prefs.customerAddress

    fun submit(name: String, phone: String, address: String, note: String) {
        if (_state.value is CheckoutUiState.Submitting) return

        val validation = validate(name, phone, address)
        if (validation != null) {
            _state.value = CheckoutUiState.Error(validation)
            return
        }

        val items = cart.items.value
        if (items.isEmpty()) {
            _state.value = CheckoutUiState.Error("Keranjang masih kosong.")
            return
        }

        _state.value = CheckoutUiState.Submitting

        viewModelScope.launch {
            runCatching {
                repository.createOrder(
                    name = name,
                    phone = phone,
                    address = address,
                    note = note,
                    items = items,
                )
            }
                .onSuccess { response ->
                    prefs.customerName = name.trim()
                    prefs.customerPhone = phone.trim()
                    prefs.customerAddress = address.trim()
                    prefs.lastOrderNumber = response.order.orderNumber
                    prefs.lastOrderPhone = phone.trim()
                    cart.clear()
                    _state.value = CheckoutUiState.Success(
                        order = response.order,
                        whatsappUrl = response.whatsappUrl ?: response.order.whatsappUrl,
                    )
                }
                .onFailure { error ->
                    _state.value = CheckoutUiState.Error(
                        error.message ?: "Gagal membuat pesanan. Coba lagi.",
                    )
                }
        }
    }

    fun reset() {
        if (_state.value !is CheckoutUiState.Submitting) {
            _state.value = CheckoutUiState.Idle
        }
    }

    private fun validate(name: String, phone: String, address: String): String? {
        if (name.trim().length < 2) return "Nama minimal 2 karakter."
        if (!isValidPhone(phone)) return "Nomor WhatsApp minimal 8 digit."
        if (address.trim().length < 5) return "Alamat minimal 5 karakter."
        return null
    }
}
