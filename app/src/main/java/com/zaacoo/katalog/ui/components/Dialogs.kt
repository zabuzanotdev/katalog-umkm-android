package com.zaacoo.katalog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.Product
import com.zaacoo.katalog.data.Promotion
import com.zaacoo.katalog.ui.theme.Gold
import com.zaacoo.katalog.ui.theme.GoldDark
import com.zaacoo.katalog.ui.theme.GoldSoft
import com.zaacoo.katalog.ui.theme.Muted
import com.zaacoo.katalog.ui.theme.Red
import com.zaacoo.katalog.ui.components.zaaTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoDetailSheet(
    promotion: Promotion,
    onDismiss: () -> Unit,
    onProductClick: (Product) -> Unit,
    onWhatsApp: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val product = promotion.product
    val image = promotion.imageUrl ?: product?.imageUrl

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding(),
        ) {
            if (!image.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp)),
                ) {
                    NetworkImage(
                        url = image,
                        contentDescription = promotion.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if ((promotion.discountPercent ?: 0) > 0) {
                Pill(
                    text = "Diskon ${promotion.discountPercent}%",
                    background = GoldSoft,
                    foreground = GoldDark,
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = promotion.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            if (!promotion.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = promotion.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }

            if (product != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onProductClick(product) },
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
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        ) {
                            NetworkImage(
                                url = product.imageUrl,
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            PriceRow(price = product.price, promoPrice = product.promoPrice)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onWhatsApp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Tanya via WhatsApp")
            }
        }
    }
}

@Composable
fun ReviewDialog(
    itemName: String,
    submitting: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
) {
    var rating by remember { mutableStateOf(5) }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Beri Ulasan") },
        text = {
            Column {
                Text(itemName, style = MaterialTheme.typography.bodyMedium, color = Muted)
                Spacer(Modifier.height(12.dp))
                Row {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 },
                            enabled = !submitting,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "${index + 1} bintang",
                                tint = if (index < rating) Gold else MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ulasan (opsional)") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !submitting,
                    colors = zaaTextFieldColors(),
                )
                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, style = MaterialTheme.typography.bodySmall, color = Red)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, text) },
                enabled = !submitting,
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Kirim")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) {
                Text("Batal")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
