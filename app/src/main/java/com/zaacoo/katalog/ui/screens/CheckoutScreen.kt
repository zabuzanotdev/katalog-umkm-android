package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.CartItem
import com.zaacoo.katalog.ui.CheckoutUiState
import com.zaacoo.katalog.ui.components.InlineNotice
import com.zaacoo.katalog.ui.components.NoticeTone
import com.zaacoo.katalog.ui.components.SimpleTopBar
import com.zaacoo.katalog.ui.components.zaaTextFieldColors
import com.zaacoo.katalog.ui.theme.Coral
import com.zaacoo.katalog.ui.theme.InkSoft
import com.zaacoo.katalog.ui.theme.Muted
import com.zaacoo.katalog.util.toRupiah

@Composable
fun CheckoutScreen(
    state: CheckoutUiState,
    items: List<CartItem>,
    initialName: String,
    initialPhone: String,
    initialAddress: String,
    onBack: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    onSuccess: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var phone by rememberSaveable { mutableStateOf(initialPhone) }
    var address by rememberSaveable { mutableStateOf(initialAddress) }
    var note by rememberSaveable { mutableStateOf("") }

    val submitting = state is CheckoutUiState.Submitting
    val error = (state as? CheckoutUiState.Error)?.message
    val total = items.sumOf { it.subtotal }

    var isFirstState by remember { mutableStateOf(true) }
    LaunchedEffect(state) {
        // Abaikan state lama saat layar baru dibuka; hanya navigasi kalau
        // Success benar-benar terjadi setelah pengguna menekan tombol pesan.
        if (isFirstState) {
            isFirstState = false
            return@LaunchedEffect
        }

        val success = state as? CheckoutUiState.Success
        if (success != null) {
            onSuccess(success.order.orderNumber)
        }
    }

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(
            title = "Checkout",
            onBack = onBack,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (error != null) {
                item {
                    InlineNotice(
                        text = error,
                        tone = NoticeTone.Error,
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Data Pemesan", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nama lengkap") },
                            singleLine = true,
                            enabled = !submitting,
                            colors = zaaTextFieldColors(),
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nomor WhatsApp") },
                            placeholder = { Text("08xxxxxxxxxx") },
                            singleLine = true,
                            enabled = !submitting,
                            colors = zaaTextFieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Alamat lengkap") },
                            minLines = 2,
                            maxLines = 4,
                            enabled = !submitting,
                            colors = zaaTextFieldColors(),
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Catatan (opsional)") },
                            placeholder = { Text("Contoh: pedas, tanpa gula, dll.") },
                            minLines = 2,
                            maxLines = 4,
                            enabled = !submitting,
                            colors = zaaTextFieldColors(),
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))

                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${item.quantity} x ${item.unitPrice.toRupiah()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Muted,
                                    )
                                }
                                Text(
                                    text = item.subtotal.toRupiah(),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(10.dp))

                        Row {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = total.toRupiah(),
                                style = MaterialTheme.typography.titleLarge,
                                color = Coral,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Pesanan akan disimpan dan diteruskan ke WhatsApp penjual. Pastikan nomor WhatsApp aktif.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                )
            }
        }

        if (items.isNotEmpty()) {
            CheckoutBottomBar(
                total = total,
                submitting = submitting,
                onSubmit = { onSubmit(name, phone, address, note) },
            )
        }
    }
}

@Composable
private fun CheckoutBottomBar(
    total: Int,
    submitting: Boolean,
    onSubmit: () -> Unit,
) {
    Surface(
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Total", style = MaterialTheme.typography.labelSmall, color = Muted)
                Text(
                    text = total.toRupiah(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Coral,
                    fontWeight = FontWeight.Black,
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !submitting,
                modifier = Modifier.height(48.dp),
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Memproses...")
                } else {
                    Text("Pesan via WhatsApp")
                }
            }
        }
    }
}
