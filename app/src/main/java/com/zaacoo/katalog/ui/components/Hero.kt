package com.zaacoo.katalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.Settings
import com.zaacoo.katalog.ui.theme.Blue800
import com.zaacoo.katalog.ui.theme.InkSoft

@Composable
fun HeroSection(
    settings: Settings?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFDF6F0),
                        Color(0xFFFCE9E4),
                        Color(0xFFF7CFC8),
                        Color(0xFFEE9A8E),
                        Color(0xFFC0392B),
                    ),
                ),
            ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = settings?.businessName ?: "Zaa&Co",
                style = MaterialTheme.typography.headlineMedium,
                color = Blue800,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = settings?.tagline ?: "Rasa lokal, kualitas terbaik",
                style = MaterialTheme.typography.titleSmall,
                color = InkSoft,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = settings?.description
                    ?: "Katalog produk UMKM pilihan. Pesan langsung lewat WhatsApp.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft.copy(alpha = 0.82f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
