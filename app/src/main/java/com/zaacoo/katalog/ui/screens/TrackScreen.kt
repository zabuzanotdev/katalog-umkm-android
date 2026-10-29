package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.Order
import com.zaacoo.katalog.data.OrderItem
import com.zaacoo.katalog.ui.TrackUiState
import com.zaacoo.katalog.ui.components.EmptyState
import com.zaacoo.katalog.ui.components.InlineNotice
import com.zaacoo.katalog.ui.components.InfoRow
import com.zaacoo.katalog.ui.components.NetworkImage
import com.zaacoo.katalog.ui.components.NoticeTone
import com.zaacoo.katalog.ui.components.RatingStars
import com.zaacoo.katalog.ui.components.ReviewDialog
import com.zaacoo.katalog.ui.components.SimpleTopBar
import com.zaacoo.katalog.ui.components.StatusBadge
import com.zaacoo.katalog.ui.components.zaaTextFieldColors
import com.zaacoo.katalog.ui.theme.Coral
import com.zaacoo.katalog.ui.theme.Green
import com.zaacoo.katalog.ui.theme.InkSoft
import com.zaacoo.katalog.ui.theme.Muted
import com.zaacoo.katalog.util.formatDateTime
import com.zaacoo.katalog.util.toRupiah

@Composable
fun TrackScreen(
    state: TrackUiState,
    onOrderNumberChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onLookup: () -> Unit,
    onStartReview: (OrderItem) -> Unit,
    onDismissReview: () -> Unit,
    onSubmitReview: (Int, String) -> Unit,
    onWhatsApp: (String?) -> Unit,
    onProductClick: (String) -> Unit,
) {
    val error = state.error
    val order = state.order

    Scaffold(
        topBar = { SimpleTopBar(title = "Lacak Pesanan") },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Cek status pesanan", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.orderNumber,
                            onValueChange = onOrderNumberChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nomor pesanan") },
                            placeholder = { Text("ORD-20260101-XXXXXX") },
                            singleLine = true,
                            enabled = !state.loading,
                            colors = zaaTextFieldColors(),
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = onPhoneChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nomor WhatsApp") },
                            placeholder = { Text("08xxxxxxxxxx") },
                            singleLine = true,
                            enabled = !state.loading,
                            colors = zaaTextFieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        )
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = onLookup,
                            enabled = !state.loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Lacak Pesanan")
                            }
                        }
                    }
                }
            }

            if (error != null) {
                item {
                    InlineNotice(
                        text = error,
                        tone = NoticeTone.Error,
                    )
                }
            }

            if (order != null) {
                item { OrderStatusCard(order = order) }
                item { StatusTimeline(order = order) }
                item { OrderItemsCard(order = order, onStartReview = onStartReview, onProductClick = onProductClick) }

                if (order.reviews.isNotEmpty()) {
                    item { OrderReviewsCard(order = order) }
                }

                if (!order.whatsappUrl.isNullOrBlank()) {
                    item {
                        Button(
                            onClick = { onWhatsApp(order.whatsappUrl) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        ) {
                            Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Chat Penjual via WhatsApp")
                        }
                    }
                }
            } else if (!state.loading && error == null) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.List,
                        title = "Belum ada pesanan",
                        message = "Masukkan nomor pesanan dan nomor WhatsApp yang kamu pakai saat checkout.",
                    )
                }
            }
        }
    }

    state.reviewItem?.let { item ->
        ReviewDialog(
            itemName = item.productName,
            submitting = state.reviewSubmitting,
            error = state.reviewError,
            onDismiss = onDismissReview,
            onSubmit = onSubmitReview,
        )
    }
}

@Composable
private fun OrderStatusCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Nomor Pesanan", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text(order.orderNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                StatusBadge(status = order.status, label = order.statusLabel)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Dibuat ${formatDateTime(order.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(6.dp))

            InfoRow(Icons.Outlined.Person, "Pemesan", order.customer.name)
            InfoRow(Icons.Outlined.Phone, "WhatsApp", order.customer.phone)
            InfoRow(Icons.Outlined.LocationOn, "Alamat", order.customer.address)
            if (!order.customer.note.isNullOrBlank()) {
                InfoRow(Icons.Outlined.Info, "Catatan", order.customer.note.orEmpty())
            }
        }
    }
}

@Composable
private fun StatusTimeline(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Riwayat Status", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            if (order.statusHistory.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(order.statusLabel, style = MaterialTheme.typography.titleSmall)
                        Text("Status saat ini", style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }
            } else {
                order.statusHistory.forEachIndexed { index, entry ->
                    Row {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                            if (index < order.statusHistory.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(30.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant),
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.padding(bottom = 8.dp)) {
                            Text(entry.toLabel ?: "-", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = formatDateTime(entry.at),
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemsCard(
    order: Order,
    onStartReview: (OrderItem) -> Unit,
    onProductClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Produk", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = item.productSlug != null) {
                                item.productSlug?.let(onProductClick)
                            },
                    ) {
                        NetworkImage(
                            url = item.imageUrl,
                            contentDescription = item.productName,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${item.quantity} x ${item.price.toRupiah()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.subtotal.toRupiah(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        when {
                            item.reviewed -> Text(
                                text = "Sudah diulas",
                                style = MaterialTheme.typography.labelSmall,
                                color = Green,
                            )
                            order.canReview -> TextButton(
                                onClick = { onStartReview(item) },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text("Ulasan", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = order.totalAmount.toRupiah(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Coral,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun OrderReviewsCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Ulasan Kamu", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            order.reviews.forEach { review ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.name,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f),
                        )
                        RatingStars(rating = review.rating.toDouble(), starSize = 14.dp)
                    }
                    if (!review.review.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = review.review,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft,
                        )
                    }
                }
            }
        }
    }
}
