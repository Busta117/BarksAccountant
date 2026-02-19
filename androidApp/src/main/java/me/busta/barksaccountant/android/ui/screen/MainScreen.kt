package me.busta.barksaccountant.android.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.barksColors
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import me.busta.barksaccountant.android.ui.screen.purchases.PurchaseFormScreen
import me.busta.barksaccountant.android.ui.screen.purchases.PurchasesListScreen
import me.busta.barksaccountant.android.ui.screen.sales.InvoiceScreen
import me.busta.barksaccountant.android.ui.screen.sales.SaleDetailScreen
import me.busta.barksaccountant.android.ui.screen.sales.SaleFormScreen
import me.busta.barksaccountant.android.ui.screen.sales.SalesListScreen
import me.busta.barksaccountant.android.ui.screen.stats.StatsScreen
import me.busta.barksaccountant.android.ui.screen.settings.BusinessInfoScreen
import me.busta.barksaccountant.android.ui.screen.settings.ClientFormScreen
import me.busta.barksaccountant.android.ui.screen.settings.ClientsListScreen
import me.busta.barksaccountant.android.ui.screen.settings.ProductFormScreen
import me.busta.barksaccountant.android.ui.screen.settings.ProductsListScreen
import me.busta.barksaccountant.android.ui.screen.settings.SettingsScreen
import me.busta.barksaccountant.di.ServiceLocator

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem("sales_list", "Ventas", Icons.Default.ShoppingCart),
    TabItem("purchases_list", "Compras", Icons.Default.LocalMall),
    TabItem("stats", "Stats", Icons.Default.BarChart),
    TabItem("settings", "Settings", Icons.Default.Settings)
)

@Composable
fun MainScreen(
    serviceLocator: ServiceLocator,
    personName: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var pendingInvoiceHtml by remember { mutableStateOf<String?>(null) }
    var pendingSummaryHtml by remember { mutableStateOf<String?>(null) }

    val colors = barksColors()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = colors.screenBackground
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selectedTab == index,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BarksLightBlue,
                            selectedTextColor = BarksLightBlue,
                            indicatorColor = BarksLightBlue.copy(alpha = 0.15f),
                            unselectedIconColor = colors.secondaryText,
                            unselectedTextColor = colors.secondaryText
                        ),
                        onClick = {
                            selectedTab = index
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "sales_list",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Sales
            composable("sales_list") {
                SalesListScreen(
                    serviceLocator = serviceLocator,
                    onSaleClick = { saleId -> navController.navigate("sale_detail/$saleId") },
                    onNewSale = { navController.navigate("sale_form") }
                )
            }

            composable(
                "sale_detail/{saleId}",
                arguments = listOf(navArgument("saleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId") ?: return@composable
                SaleDetailScreen(
                    serviceLocator = serviceLocator,
                    saleId = saleId,
                    onEditSale = { navController.navigate("sale_form?saleId=$saleId") },
                    onBack = { navController.popBackStack() },
                    onInvoiceReady = { html, invoiceSaleId ->
                        pendingInvoiceHtml = html
                        navController.navigate("invoice/$invoiceSaleId")
                    },
                    onSummaryReady = { html, summarySaleId ->
                        pendingSummaryHtml = html
                        navController.navigate("summary/$summarySaleId")
                    }
                )
            }

            composable(
                "sale_form?saleId={saleId}",
                arguments = listOf(navArgument("saleId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId")
                SaleFormScreen(
                    serviceLocator = serviceLocator,
                    saleId = saleId,
                    personName = personName,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                "invoice/{saleId}",
                arguments = listOf(navArgument("saleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val invoiceSaleId = backStackEntry.arguments?.getString("saleId") ?: return@composable
                val html = pendingInvoiceHtml ?: return@composable
                InvoiceScreen(
                    invoiceHtml = html,
                    saleId = invoiceSaleId,
                    onBack = {
                        pendingInvoiceHtml = null
                        navController.popBackStack()
                    }
                )
            }

            composable(
                "summary/{saleId}",
                arguments = listOf(navArgument("saleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val summarySaleId = backStackEntry.arguments?.getString("saleId") ?: return@composable
                val html = pendingSummaryHtml ?: return@composable
                InvoiceScreen(
                    invoiceHtml = html,
                    saleId = summarySaleId,
                    documentName = "Resumen",
                    onBack = {
                        pendingSummaryHtml = null
                        navController.popBackStack()
                    }
                )
            }

            // Purchases
            composable("purchases_list") {
                PurchasesListScreen(
                    serviceLocator = serviceLocator,
                    onPurchaseClick = { purchaseId -> navController.navigate("purchase_form?purchaseId=$purchaseId") },
                    onNewPurchase = { navController.navigate("purchase_form") }
                )
            }

            composable(
                "purchase_form?purchaseId={purchaseId}",
                arguments = listOf(navArgument("purchaseId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val purchaseId = backStackEntry.arguments?.getString("purchaseId")
                PurchaseFormScreen(
                    serviceLocator = serviceLocator,
                    purchaseId = purchaseId,
                    personName = personName,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            // Stats
            composable("stats") {
                StatsScreen(serviceLocator = serviceLocator)
            }

            // Settings
            composable("settings") {
                SettingsScreen(
                    serviceLocator = serviceLocator,
                    personName = personName,
                    onLogout = onLogout,
                    onProductsClick = { navController.navigate("products_list") },
                    onClientsClick = { navController.navigate("clients_list") },
                    onBusinessInfoClick = { navController.navigate("business_info") }
                )
            }

            composable("products_list") {
                ProductsListScreen(
                    serviceLocator = serviceLocator,
                    onProductClick = { productId -> navController.navigate("product_form?productId=$productId") },
                    onNewProduct = { navController.navigate("product_form") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                "product_form?productId={productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                ProductFormScreen(
                    serviceLocator = serviceLocator,
                    productId = productId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("clients_list") {
                ClientsListScreen(
                    serviceLocator = serviceLocator,
                    onClientClick = { clientId -> navController.navigate("client_form?clientId=$clientId") },
                    onNewClient = { navController.navigate("client_form") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                "client_form?clientId={clientId}",
                arguments = listOf(navArgument("clientId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")
                ClientFormScreen(
                    serviceLocator = serviceLocator,
                    clientId = clientId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("business_info") {
                BusinessInfoScreen(
                    serviceLocator = serviceLocator,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
