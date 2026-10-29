package com.zaacoo.katalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaacoo.katalog.data.Product
import com.zaacoo.katalog.data.Settings
import com.zaacoo.katalog.ui.CatalogUiState
import com.zaacoo.katalog.ui.components.BrandTopBar
import com.zaacoo.katalog.ui.components.EmptyState
import com.zaacoo.katalog.ui.components.ErrorState
import com.zaacoo.katalog.ui.components.InlineNotice
import com.zaacoo.katalog.ui.components.NoticeTone
import com.zaacoo.katalog.ui.components.ProductCard
import com.zaacoo.katalog.ui.components.SectionHeader
import com.zaacoo.katalog.ui.components.zaaTextFieldColors
import com.zaacoo.katalog.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogUiState,
    settings: Settings?,
    onRefresh: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    onProductClick: (Product) -> Unit,
    onLoadMore: () -> Unit,
) {
    val error = state.error

    Column(Modifier.fillMaxSize()) {
        BrandTopBar(settings = settings)

        Column(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari produk...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = zaaTextFieldColors(),
            )

            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CatalogFilterChip(
                        selected = state.category.isEmpty(),
                        onClick = { onCategoryChange("") },
                        label = "Semua",
                    )
                }
                items(state.categories, key = { "chip-${it.id}" }) { category ->
                    CatalogFilterChip(
                        selected = state.category == category.slug,
                        onClick = { onCategoryChange(category.slug) },
                        label = category.name,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CatalogFilterChip(
                        selected = state.sort == "latest",
                        onClick = { onSortChange("latest") },
                        label = "Terbaru",
                    )
                }
                item {
                    CatalogFilterChip(
                        selected = state.sort == "cheapest",
                        onClick = { onSortChange("cheapest") },
                        label = "Termurah",
                    )
                }
                item {
                    CatalogFilterChip(
                        selected = state.sort == "priciest",
                        onClick = { onSortChange("priciest") },
                        label = "Termahal",
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = state.loading && state.products.isNotEmpty(),
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            if (error != null && state.products.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    InlineNotice(
                        text = error,
                        tone = NoticeTone.Warning,
                        actionLabel = "Coba lagi",
                        onAction = onRefresh,
                    )
                }
            }

            if (state.loading && state.products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!state.loading && state.products.isEmpty() && error != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorState(message = error, onRetry = onRefresh)
                }
            }

            if (!state.loading && state.products.isEmpty() && error == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = "Produk tidak ditemukan",
                        message = "Coba kata kunci atau kategori lain.",
                        actionLabel = "Reset filter",
                        onAction = onResetFilters,
                    )
                }
            }

            items(state.products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                )
            }

            if (state.soldOut.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(
                        title = "Stok Habis",
                        subtitle = "Produk yang sedang tidak tersedia",
                    )
                }
                items(state.soldOut, key = { "sold-${it.id}" }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) },
                    )
                }
            }

            if (state.hasMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    if (error != null && !state.loadingMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            TextButton(onClick = onLoadMore) {
                                Text("Coba lagi memuat produk")
                            }
                        }
                    } else {
                        LaunchedEffect(state.page) {
                            onLoadMore()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }

            if (state.products.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "${state.total} produk ditemukan",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun CatalogFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}
