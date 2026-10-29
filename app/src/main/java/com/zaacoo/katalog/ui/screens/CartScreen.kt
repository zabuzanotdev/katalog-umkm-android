package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.CartItem
import com.zaacoo.katalog.ui.components.ConfirmDialog
import com.zaacoo.katalog.ui.components.EmptyState
import com.zaacoo.katalog.ui.components.NetworkImage
import com.zaacoo.katalog.ui.components.PriceRow
import com.zaacoo.katalog.ui.components.QtyStepper
import com.zaacoo.katalog.ui.components.SimpleTopBar
import com.zaacoo.katalog.ui.theme.Coral
import com.zaacoo.katalog.ui.theme.InkSoft
import com.zaacoo.katalog.ui.theme.Muted
import com.zaacoo.katalog.util.toRupiah

@Composable
fun CartScreen(
    items: List<CartItem>,
    onQuantityChange: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    onCheckout: () -> Unit,
    onBrowse: () -> Unit,
    onProductClick: (String) -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    val total = items.sumOf { it.subtotal }

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(
            title = "Keranjang",
            actions = {
                if (items.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Text("Kosongkan", color = Coral)
                    }
                }
            },
        )

        Box(Modifier.weight(1f)) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "Keranjang masih kosong",
                        message = "Yuk, pilih produk favoritmu dulu.",
                        actionLabel = "Mulai Belanja",
                        onAction = onBrowse,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                items(items, key = { it.productId }) { item ->
                    CartRow(
                        item = item,
                        onProductClick = { onProductClick(item.slug) },
                        onQuantityChange = { quantity -> onQuantityChange(item.productId, quantity) },
                        onRemove = { onRemove(item.productId) },
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Ringkasan", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(10.dp))
                            Row {
                                Text(
                                    text = "Subtotal (${items.sumOf { it.quantity }} item)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Muted,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(total.toRupiah(), style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(10.dp))
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
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Harga dan stok dihitung ulang saat checkout.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                    }
                }
            }
            }
        }

        if (items.isNotEmpty()) {
            CartBottomBar(total = total, onCheckout = onCheckout)
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Kosongkan keranjang?",
            message = "Semua produk di keranjang akan dihapus.",
            confirmLabel = "Kosongkan",
            onConfirm = {
                showClearDialog = false
                onClear()
            },
            onDismiss = { showClearDialog = false },
        )
    }
}

@Composable
private fun CartRow(
    item: CartItem,
    onProductClick: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onProductClick),
            ) {
                NetworkImage(
                    url = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onProductClick),
                )
                Spacer(Modifier.height(4.dp))
                PriceRow(price = item.price, promoPrice = item.promoPrice)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QtyStepper(
                        quantity = item.quantity,
                        max = item.stock.coerceAtLeast(1),
                        onMinus = { onQuantityChange(item.quantity - 1) },
                        onPlus = { onQuantityChange(item.quantity + 1) },
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Hapus ${item.name}",
                            tint = Coral,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartBottomBar(total: Int, onCheckout: () -> Unit) {
    Surface(
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                onClick = onCheckout,
                modifier = Modifier.height(48.dp),
            ) {
                Text("Checkout")
            }
        }
    }
}
