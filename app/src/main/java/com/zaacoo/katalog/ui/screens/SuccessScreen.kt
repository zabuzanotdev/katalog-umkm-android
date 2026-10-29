package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.ui.CheckoutUiState
import com.zaacoo.katalog.ui.components.SimpleTopBar
import com.zaacoo.katalog.ui.components.StatusBadge
import com.zaacoo.katalog.ui.theme.Coral
import com.zaacoo.katalog.ui.theme.Green
import com.zaacoo.katalog.ui.theme.GreenSoft
import com.zaacoo.katalog.ui.theme.Muted
import com.zaacoo.katalog.util.toRupiah
import kotlinx.coroutines.launch

@Composable
fun SuccessScreen(
    orderNumber: String,
    state: CheckoutUiState,
    onHome: () -> Unit,
    onTrack: () -> Unit,
    onOpenWhatsApp: (String?) -> Unit,
) {
    val success = state as? CheckoutUiState.Success
    val order = success?.order
    val whatsappUrl = success?.whatsappUrl ?: order?.whatsappUrl
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Pesanan Berhasil",
                onBack = onHome,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(GreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = Green,
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Pesanan berhasil dibuat!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Pesananmu sudah tercatat. Lanjutkan ke WhatsApp supaya penjual bisa konfirmasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Nomor Pesanan", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = order?.orderNumber ?: orderNumber,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                    TextButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(order?.orderNumber ?: orderNumber))
                            scope.launch { snackbar.showSnackbar("Nomor pesanan disalin") }
                        },
                    ) {
                        Text("Salin nomor pesanan")
                    }

                    if (order != null) {
                        Spacer(Modifier.height(8.dp))
                        StatusBadge(status = order.status, label = order.statusLabel)
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = order.totalAmount.toRupiah(),
                                style = MaterialTheme.typography.titleLarge,
                                color = Coral,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${order.items.sumOf { it.quantity }} item",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onOpenWhatsApp(whatsappUrl) },
                enabled = !whatsappUrl.isNullOrBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text("Buka WhatsApp")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onTrack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text("Lacak Pesanan")
            }

            Spacer(Modifier.height(6.dp))

            TextButton(onClick = onHome) {
                Text("Kembali ke Beranda")
            }

            if (order == null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Detail pesanan tidak tersedia di sesi ini. Buka Lacak Pesanan dengan nomor di atas untuk melihat statusnya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
