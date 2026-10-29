package com.zaacoo.katalog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.zaacoo.katalog.data.Settings
import com.zaacoo.katalog.ui.theme.Amber
import com.zaacoo.katalog.ui.theme.AmberSoft
import com.zaacoo.katalog.ui.theme.Blue50
import com.zaacoo.katalog.ui.theme.Blue700
import com.zaacoo.katalog.ui.theme.Coral
import com.zaacoo.katalog.ui.theme.Gold
import com.zaacoo.katalog.ui.theme.Green
import com.zaacoo.katalog.ui.theme.Neutral950
import com.zaacoo.katalog.ui.theme.GreenSoft
import com.zaacoo.katalog.ui.theme.Indigo
import com.zaacoo.katalog.ui.theme.IndigoSoft
import com.zaacoo.katalog.ui.theme.Muted
import com.zaacoo.katalog.ui.theme.Red
import com.zaacoo.katalog.ui.theme.RedSoft
import com.zaacoo.katalog.util.toRupiah
import kotlin.math.roundToInt

@Composable
fun BrandLogo(url: String?, size: Dp = 42.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(Blue700),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = "Logo toko",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { BrandLogoFallback() },
                error = { BrandLogoFallback() },
            )
        } else {
            BrandLogoFallback()
        }
    }
}

@Composable
private fun BrandLogoFallback() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Z",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
        )
    }
}

@Composable
fun BrandTopBar(settings: Settings?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandLogo(settings?.logoUrl)

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = settings?.businessName ?: "Zaa&Co",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Katalog UMKM",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartIconButton(cartCount: Int, onClick: () -> Unit) {
    BadgedBox(
        badge = {
            if (cartCount > 0) {
                Badge(containerColor = Gold, contentColor = Neutral950) {
                    Text(if (cartCount > 99) "99+" else cartCount.toString())
                }
            }
        },
    ) {
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Keranjang")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Kembali")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun NetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (url.isNullOrBlank()) {
        ImageFallback(modifier)
        return
    }

    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = { ImageFallback(Modifier.fillMaxSize()) },
        error = { ImageFallback(Modifier.fillMaxSize()) },
    )
}

@Composable
private fun ImageFallback(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surface,
                ),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Z",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
fun PriceRow(
    price: Int,
    promoPrice: Int?,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val hasPromo = promoPrice != null && promoPrice < price

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = (promoPrice ?: price).toRupiah(),
            style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (hasPromo) Coral else MaterialTheme.colorScheme.onSurface,
        )
        if (hasPromo) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = price.toRupiah(),
                style = MaterialTheme.typography.labelSmall,
                textDecoration = TextDecoration.LineThrough,
                color = Muted,
            )
        }
    }
}

@Composable
fun Pill(
    text: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = foreground,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
    )
}

@Composable
fun StockBadge(stock: Int, modifier: Modifier = Modifier) {
    when {
        stock <= 0 -> Pill("Stok habis", RedSoft, Red, modifier)
        stock <= 5 -> Pill("Sisa $stock", AmberSoft, Amber, modifier)
        else -> Pill("Stok $stock", GreenSoft, Green, modifier)
    }
}

@Composable
fun RatingStars(
    rating: Double,
    count: Int? = null,
    starSize: Dp = 16.dp,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val filled = rating.roundToInt().coerceIn(0, 5)
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = if (index < filled) Gold else MaterialTheme.colorScheme.outlineVariant,
            )
        }
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = String.format(java.util.Locale.US, "%.1f (%d)", rating, count),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
    }
}

@Composable
fun QtyStepper(
    quantity: Int,
    max: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton("−", enabled = quantity > 1, onClick = onMinus)
        Text(
            text = quantity.toString(),
            modifier = Modifier.widthIn(min = 28.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        StepperButton("+", enabled = quantity < max, onClick = onPlus)
    }
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Warning,
        title = "Gagal memuat",
        message = message,
        actionLabel = "Coba lagi",
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

enum class NoticeTone { Info, Warning, Error }

@Composable
fun InlineNotice(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val (background, foreground) = when (tone) {
        NoticeTone.Info -> Blue50 to Blue700
        NoticeTone.Warning -> AmberSoft to Amber
        NoticeTone.Error -> RedSoft to Red
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, foreground.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Warning, contentDescription = null, modifier = Modifier.size(18.dp), tint = foreground)
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = foreground,
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = foreground)
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun zaaTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
fun StatusBadge(status: String, label: String, modifier: Modifier = Modifier) {
    val (background, foreground) = when (status) {
        "completed" -> GreenSoft to Green
        "cancelled" -> RedSoft to Red
        "processing" -> IndigoSoft to Indigo
        "contacted" -> Blue50 to Blue700
        else -> AmberSoft to Amber
    }
    Pill(label, background, foreground, modifier)
}
