package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.BuildConfig
import com.zaacoo.katalog.data.Settings
import com.zaacoo.katalog.ui.components.BrandLogo
import com.zaacoo.katalog.ui.components.BrandTopBar
import com.zaacoo.katalog.ui.components.InfoRow
import com.zaacoo.katalog.ui.theme.InkSoft
import com.zaacoo.katalog.ui.theme.Muted

@Composable
fun AboutScreen(
    settings: Settings?,
    onWhatsApp: (String?) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        BrandTopBar(settings = settings)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BrandLogo(url = settings?.logoUrl, size = 92.dp)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = settings?.businessName ?: "Zaa&Co",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                    if (!settings?.tagline.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = settings?.tagline.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                            textAlign = TextAlign.Center,
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
                        Text("Tentang Kami", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = settings?.description
                                ?: "UMKM lokal yang menjual produk pilihan dengan harga bersahabat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft,
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
                        Text("Informasi Toko", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))

                        if (!settings?.address.isNullOrBlank()) {
                            InfoRow(Icons.Outlined.LocationOn, "Alamat", settings?.address.orEmpty())
                        }
                        if (!settings?.openingHours.isNullOrBlank()) {
                            InfoRow(Icons.Outlined.DateRange, "Jam buka", settings?.openingHours.orEmpty())
                        }
                        if (!settings?.phone.isNullOrBlank()) {
                            InfoRow(Icons.Outlined.Phone, "Telepon", settings?.phone.orEmpty())
                        }
                        if (!settings?.email.isNullOrBlank()) {
                            InfoRow(Icons.Outlined.Email, "Email", settings?.email.orEmpty())
                        }
                        if (!settings?.instagram.isNullOrBlank()) {
                            InfoRow(Icons.Outlined.Share, "Instagram", settings?.instagram.orEmpty())
                        }
                        if (!settings?.facebook.isNullOrBlank()) {
                            InfoRow(Icons.Outlined.Share, "Facebook", settings?.facebook.orEmpty())
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { onWhatsApp(settings?.whatsappChatUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chat WhatsApp")
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
                        Text("Aplikasi", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        InfoRow(
                            Icons.Outlined.Info,
                            "Versi",
                            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        )
                        Text(
                            text = "Aplikasi Android resmi Katalog UMKM. Dibuat ringan supaya belanja produk lokal makin mudah.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }
}
