package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.Category
import com.zaacoo.katalog.data.Product
import com.zaacoo.katalog.data.Promotion
import com.zaacoo.katalog.ui.HomeUiState
import com.zaacoo.katalog.ui.components.BrandTopBar
import com.zaacoo.katalog.ui.components.CategoryCard
import com.zaacoo.katalog.ui.components.ErrorState
import com.zaacoo.katalog.ui.components.HeroSection
import com.zaacoo.katalog.ui.components.InlineNotice
import com.zaacoo.katalog.ui.components.LoadingState
import com.zaacoo.katalog.ui.components.NoticeTone
import com.zaacoo.katalog.ui.components.ProductCard
import com.zaacoo.katalog.ui.components.ProductMiniCard
import com.zaacoo.katalog.ui.components.PromoCard
import com.zaacoo.katalog.ui.components.PromoDetailSheet
import com.zaacoo.katalog.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onBrowse: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onProductClick: (Product) -> Unit,
    onWhatsApp: (String?) -> Unit,
) {
    val content = state as? HomeUiState.Content
    val settings = content?.home?.settings

    Column(Modifier.fillMaxSize()) {
        BrandTopBar(settings = settings)

        when (state) {
            HomeUiState.Loading -> LoadingState()
            is HomeUiState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(
                    message = state.message,
                    onRetry = onRefresh,
                )
            }
            is HomeUiState.Content -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                HomeContent(
                    content = content ?: state,
                    onRefresh = onRefresh,
                    onBrowse = onBrowse,
                    onCategoryClick = onCategoryClick,
                    onProductClick = onProductClick,
                    onWhatsApp = onWhatsApp,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    content: HomeUiState.Content,
    onRefresh: () -> Unit,
    onBrowse: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onProductClick: (Product) -> Unit,
    onWhatsApp: (String?) -> Unit,
) {
    val home = content.home
    val settings = home.settings
    val promos = remember(home.promoProducts, home.promoAnnouncements) {
        home.promoProducts + home.promoAnnouncements
    }
    var selectedPromo by remember { mutableStateOf<Promotion?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (content.fromCache && content.message != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                InlineNotice(
                    text = "Menampilkan data tersimpan. ${content.message}",
                    tone = NoticeTone.Warning,
                    actionLabel = "Muat ulang",
                    onAction = onRefresh,
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            HeroSection(settings = settings)
        }

        if (promos.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Promo",
                    subtitle = "Penawaran yang sedang berjalan",
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(promos, key = { "promo-${it.id}" }) { promotion ->
                        PromoCard(
                            promotion = promotion,
                            onClick = { selectedPromo = promotion },
                        )
                    }
                }
            }
        }

        if (home.categories.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Kategori",
                    subtitle = "Temukan produk favoritmu",
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(home.categories, key = { "cat-${it.id}" }) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category) },
                        )
                    }
                }
            }
        }

        if (home.featured.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Produk Unggulan",
                    subtitle = "Pilihan terbaik dari toko",
                    actionLabel = "Lihat semua",
                    onAction = onBrowse,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(home.featured, key = { "featured-${it.id}" }) { product ->
                        ProductMiniCard(
                            product = product,
                            onClick = { onProductClick(product) },
                        )
                    }
                }
            }
        }

        if (home.bestSellers.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Paling Laris",
                    subtitle = "Produk yang paling sering dipesan",
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(home.bestSellers, key = { "best-${it.id}" }) { product ->
                        ProductMiniCard(
                            product = product,
                            onClick = { onProductClick(product) },
                        )
                    }
                }
            }
        }

        if (home.latest.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Produk Terbaru",
                    subtitle = "${home.latest.size} produk pilihan",
                    actionLabel = "Lihat semua",
                    onAction = onBrowse,
                )
            }
            items(home.latest, key = { "latest-${it.id}" }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                )
            }
        }

        if (home.soldOut.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    title = "Stok Habis",
                    subtitle = "Pantau lagi nanti",
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(home.soldOut, key = { "sold-${it.id}" }) { product ->
                        ProductMiniCard(
                            product = product,
                            onClick = { onProductClick(product) },
                        )
                    }
                }
            }
        }
    }

    selectedPromo?.let { promotion ->
        PromoDetailSheet(
            promotion = promotion,
            onDismiss = { selectedPromo = null },
            onProductClick = { product ->
                selectedPromo = null
                onProductClick(product)
            },
            onWhatsApp = {
                selectedPromo = null
                onWhatsApp(settings.whatsappChatUrl)
            },
        )
    }
}
