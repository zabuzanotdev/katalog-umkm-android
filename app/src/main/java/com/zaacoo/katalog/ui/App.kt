package com.zaacoo.katalog.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zaacoo.katalog.AppContainer
import com.zaacoo.katalog.ui.screens.AboutScreen
import com.zaacoo.katalog.ui.screens.CartScreen
import com.zaacoo.katalog.ui.screens.CatalogScreen
import com.zaacoo.katalog.ui.screens.CheckoutScreen
import com.zaacoo.katalog.ui.screens.HomeScreen
import com.zaacoo.katalog.ui.screens.ProductScreen
import com.zaacoo.katalog.ui.screens.SuccessScreen
import com.zaacoo.katalog.ui.screens.TrackScreen
import com.zaacoo.katalog.ui.theme.Gold
import com.zaacoo.katalog.ui.theme.Neutral950
import com.zaacoo.katalog.util.openUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZaaCoApp(container: AppContainer) {
    val appVm: AppViewModel = viewModel(
        factory = createFactory<AppViewModel> { AppViewModel(container.repository, container.cart) },
    )
    val catalogVm: CatalogViewModel = viewModel(
        factory = createFactory<CatalogViewModel> { CatalogViewModel(container.repository) },
    )
    val checkoutVm: CheckoutViewModel = viewModel(
        factory = createFactory<CheckoutViewModel> {
            CheckoutViewModel(container.repository, container.cart, container.prefs)
        },
    )
    val trackVm: TrackViewModel = viewModel(
        factory = createFactory<TrackViewModel> { TrackViewModel(container.repository, container.prefs) },
    )

    val homeState by appVm.homeState.collectAsStateWithLifecycle()
    val cartItems by appVm.cartItems.collectAsStateWithLifecycle()
    val catalogState by catalogVm.state.collectAsStateWithLifecycle()
    val checkoutState by checkoutVm.state.collectAsStateWithLifecycle()
    val trackState by trackVm.state.collectAsStateWithLifecycle()

    val settings = (homeState as? HomeUiState.Content)?.home?.settings
    val cartCount = cartItems.sumOf { it.quantity }
    val context = LocalContext.current

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf("home", "catalog", "cart", "track", "about")

    fun openWhatsApp(url: String?) {
        openUrl(context, url)
    }

    fun goTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                    ) {
                    BottomNavItem(
                        label = "Beranda",
                        icon = Icons.Outlined.Home,
                        selected = currentRoute == "home",
                        onClick = { goTopLevel("home") },
                    )
                    BottomNavItem(
                        label = "Produk",
                        icon = Icons.Outlined.Search,
                        selected = currentRoute == "catalog",
                        onClick = { goTopLevel("catalog") },
                    )
                    BottomNavItem(
                        label = "Keranjang",
                        icon = Icons.Outlined.ShoppingCart,
                        selected = currentRoute == "cart",
                        badge = cartCount,
                        onClick = { goTopLevel("cart") },
                    )
                    BottomNavItem(
                        label = "Pesanan",
                        icon = Icons.Outlined.List,
                        selected = currentRoute == "track",
                        onClick = { goTopLevel("track") },
                    )
                    BottomNavItem(
                        label = "Tentang",
                        icon = Icons.Outlined.Info,
                        selected = currentRoute == "about",
                        onClick = { goTopLevel("about") },
                    )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(
                    state = homeState,
                    onRefresh = appVm::refresh,
                    onBrowse = {
                        catalogVm.setCategory("")
                        goTopLevel("catalog")
                    },
                    onCategoryClick = { category ->
                        catalogVm.setCategory(category.slug)
                        goTopLevel("catalog")
                    },
                    onProductClick = { product ->
                        navController.navigate("product/${Uri.encode(product.slug)}")
                    },
                    onWhatsApp = { url -> openWhatsApp(url) },
                )
            }

            composable("catalog") {
                CatalogScreen(
                    state = catalogState,
                    settings = settings,
                    onRefresh = catalogVm::retry,
                    onQueryChange = catalogVm::setQuery,
                    onCategoryChange = catalogVm::setCategory,
                    onSortChange = catalogVm::setSort,
                    onResetFilters = catalogVm::resetFilters,
                    onProductClick = { product ->
                        navController.navigate("product/${Uri.encode(product.slug)}")
                    },
                    onLoadMore = catalogVm::loadMore,
                )
            }

            composable("product/{slug}") { entry ->
                val slug = entry.arguments?.getString("slug").orEmpty()
                val productVm: ProductViewModel = viewModel(
                    key = "product-$slug",
                    factory = createFactory<ProductViewModel> { ProductViewModel(container.repository, slug) },
                )
                val productState by productVm.state.collectAsStateWithLifecycle()

                ProductScreen(
                    state = productState,
                    cartCount = cartCount,
                    onBack = { navController.popBackStack() },
                    onCartClick = { goTopLevel("cart") },
                    onRetry = productVm::load,
                    onAddToCart = appVm::addToCart,
                    onProductClick = { product ->
                        navController.navigate("product/${Uri.encode(product.slug)}")
                    },
                    onWhatsApp = { openWhatsApp(settings?.whatsappChatUrl) },
                )
            }

            composable("cart") {
                CartScreen(
                    items = cartItems,
                    onQuantityChange = appVm::setQuantity,
                    onRemove = appVm::removeFromCart,
                    onClear = appVm::clearCart,
                    onCheckout = { navController.navigate("checkout") },
                    onBrowse = {
                        catalogVm.setCategory("")
                        goTopLevel("catalog")
                    },
                    onProductClick = { slug ->
                        navController.navigate("product/${Uri.encode(slug)}")
                    },
                )
            }

            composable("checkout") {
                LaunchedEffect(Unit) {
                    checkoutVm.reset()
                }

                CheckoutScreen(
                    state = checkoutState,
                    items = cartItems,
                    initialName = checkoutVm.initialName,
                    initialPhone = checkoutVm.initialPhone,
                    initialAddress = checkoutVm.initialAddress,
                    onBack = { navController.popBackStack() },
                    onSubmit = checkoutVm::submit,
                    onSuccess = { orderNumber ->
                        navController.navigate("success/${Uri.encode(orderNumber)}") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable("success/{orderNumber}") { entry ->
                val orderNumber = entry.arguments?.getString("orderNumber").orEmpty()

                SuccessScreen(
                    orderNumber = orderNumber,
                    state = checkoutState,
                    onHome = { goTopLevel("home") },
                    onTrack = {
                        val order = (checkoutState as? CheckoutUiState.Success)?.order
                        trackVm.setOrderNumber(order?.orderNumber ?: container.prefs.lastOrderNumber)
                        trackVm.setPhone(order?.customer?.phone ?: container.prefs.lastOrderPhone)
                        trackVm.lookup()
                        goTopLevel("track")
                    },
                    onOpenWhatsApp = { url -> openWhatsApp(url) },
                )
            }

            composable("track") {
                TrackScreen(
                    state = trackState,
                    onOrderNumberChange = trackVm::setOrderNumber,
                    onPhoneChange = trackVm::setPhone,
                    onLookup = trackVm::lookup,
                    onStartReview = trackVm::startReview,
                    onDismissReview = trackVm::dismissReview,
                    onSubmitReview = trackVm::submitReview,
                    onWhatsApp = { url -> openWhatsApp(url) },
                    onProductClick = { slug ->
                        navController.navigate("product/${Uri.encode(slug)}")
                    },
                )
            }

            composable("about") {
                AboutScreen(
                    settings = settings,
                    onWhatsApp = { url -> openWhatsApp(url) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            if (badge > 0) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = Gold, contentColor = Neutral950) {
                            Text(if (badge > 99) "99+" else badge.toString())
                        }
                    },
                ) {
                    Icon(icon, contentDescription = label)
                }
            } else {
                Icon(icon, contentDescription = label)
            }
        },
        label = {
            Text(label, maxLines = 1)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        alwaysShowLabel = true,
    )
}
