package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.Product
import com.zaacoo.katalog.data.ProductDetailResponse
import com.zaacoo.katalog.data.Review
import com.zaacoo.katalog.ui.ProductUiState
import com.zaacoo.katalog.ui.components.CartIconButton
import com.zaacoo.katalog.ui.components.ErrorState
import com.zaacoo.katalog.ui.components.LoadingState
import com.zaacoo.katalog.ui.components.NetworkImage
import com.zaacoo.katalog.ui.components.Pill
import com.zaacoo.katalog.ui.components.PriceRow
import com.zaacoo.katalog.ui.components.ProductMiniCard
import com.zaacoo.katalog.ui.components.QtyStepper
import com.zaacoo.katalog.ui.components.RatingStars
import com.zaacoo.katalog.ui.components.SectionHeader
import com.zaacoo.katalog.ui.components.SimpleTopBar
import com.zaacoo.katalog.ui.components.StockBadge
import com.zaacoo.katalog.ui.theme.Coral
import com.zaacoo.katalog.ui.theme.GoldDark
import com.zaacoo.katalog.ui.theme.GoldSoft
import com.zaacoo.katalog.ui.theme.InkSoft
import com.zaacoo.katalog.ui.theme.Muted
import com.zaacoo.katalog.util.formatDate
import com.zaacoo.katalog.util.toRupiah
import kotlinx.coroutines.launch

@Composable
fun ProductScreen(
    state: ProductUiState,
    cartCount: Int,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    onRetry: () -> Unit,
    onAddToCart: (Product, Int) -> Unit,
    onProductClick: (Product) -> Unit,
    onWhatsApp: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val detail = (state as? ProductUiState.Content)?.detail
    var quantity by rememberSaveable(detail?.product?.id) { mutableStateOf(1) }

    val maxQuantity = detail?.product?.stock?.coerceAtLeast(1) ?: 1
    LaunchedEffect(maxQuantity) {
        if (quantity > maxQuantity) quantity = maxQuantity
    }

    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(
            title = "Detail Produk",
            onBack = onBack,
            actions = { CartIconButton(cartCount = cartCount, onClick = onCartClick) },
        )

        Box(Modifier.weight(1f)) {
            when (state) {
                ProductUiState.Loading -> LoadingState()
                is ProductUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorState(
                        message = state.message,
                        onRetry = onRetry,
                    )
                }
                is ProductUiState.Content -> ProductContent(
                    detail = state.detail,
                    quantity = quantity,
                    onQuantityChange = { quantity = it },
                    onProductClick = onProductClick,
                    onWhatsApp = onWhatsApp,
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
            )
        }

        if (detail != null) {
            ProductBottomBar(
                product = detail.product,
                quantity = quantity,
                onAddToCart = {
                    onAddToCart(detail.product, quantity)
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Ditambahkan ke keranjang",
                            actionLabel = "Lihat",
                        )
                        if (result == SnackbarResult.ActionPerformed) onCartClick()
                    }
                },
            )
        }
    }
}

@Composable
private fun ProductContent(
    detail: ProductDetailResponse,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onProductClick: (Product) -> Unit,
    onWhatsApp: () -> Unit,
) {
    val product = detail.product

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.12f),
            ) {
                NetworkImage(
                    url = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                )
                if (product.hasPromo) {
                    Pill(
                        text = "-${product.discountPercent ?: 0}%",
                        background = Coral,
                        foreground = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                    )
                }
            }
        }

        item {
            Column(Modifier.padding(16.dp)) {
                Pill(
                    text = product.category?.name ?: "Produk",
                    background = MaterialTheme.colorScheme.primaryContainer,
                    foreground = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (detail.rating.count > 0) {
                    Spacer(Modifier.height(6.dp))
                    RatingStars(
                        rating = detail.rating.average ?: 0.0,
                        count = detail.rating.count,
                    )
                }
                Spacer(Modifier.height(10.dp))
                PriceRow(
                    price = product.price,
                    promoPrice = product.promoPrice,
                    large = true,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockBadge(product.stock)
                    if (product.isFeatured) {
                        Spacer(Modifier.width(8.dp))
                        Pill("Unggulan", GoldSoft, GoldDark)
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(14.dp))

                Text("Deskripsi", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = product.description ?: "Belum ada deskripsi untuk produk ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                )

                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Jumlah",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    QtyStepper(
                        quantity = quantity,
                        max = product.stock.coerceAtLeast(1),
                        onMinus = { if (quantity > 1) onQuantityChange(quantity - 1) },
                        onPlus = { if (quantity < product.stock) onQuantityChange(quantity + 1) },
                    )
                }
            }
        }

        if (detail.related.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Produk Serupa",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(detail.related, key = { "related-${it.id}" }) { related ->
                        ProductMiniCard(
                            product = related,
                            onClick = { onProductClick(related) },
                        )
                    }
                }
            }
        }

        if (detail.reviews.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Ulasan Pembeli",
                    subtitle = "${detail.rating.count} ulasan",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            items(detail.reviews, key = { "review-${it.id}" }) { review ->
                ReviewCard(
                    review = review,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onWhatsApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tanya produk via WhatsApp")
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                RatingStars(rating = review.rating.toDouble(), starSize = 14.dp)
            }
            if (!review.review.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = review.review,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatDate(review.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
    }
}

@Composable
private fun ProductBottomBar(
    product: Product,
    quantity: Int,
    onAddToCart: () -> Unit,
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
                    text = (product.unitPrice * quantity).toRupiah(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Coral,
                    fontWeight = FontWeight.Black,
                )
            }

            Button(
                onClick = onAddToCart,
                enabled = product.stock > 0,
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (product.stock > 0) "Tambah" else "Stok Habis")
            }
        }
    }
}
