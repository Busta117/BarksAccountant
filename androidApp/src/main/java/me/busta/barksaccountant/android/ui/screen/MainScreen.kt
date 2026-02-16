package me.busta.barksaccountant.android.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import me.busta.barksaccountant.android.ui.screen.sales.SaleDetailScreen
import me.busta.barksaccountant.android.ui.screen.sales.SaleFormScreen
import me.busta.barksaccountant.android.ui.screen.sales.SalesListScreen
import me.busta.barksaccountant.di.ServiceLocator

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem("sales_list", "Ventas", Icons.Default.ShoppingCart),
    TabItem("purchases", "Compras", Icons.Default.LocalMall),
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selectedTab == index,
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
            composable("sales_list") {
                SalesListScreen(
                    serviceLocator = serviceLocator,
                    onSaleClick = { saleId ->
                        navController.navigate("sale_detail/$saleId")
                    },
                    onNewSale = {
                        navController.navigate("sale_form")
                    }
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
                    onEditSale = {
                        navController.navigate("sale_form?saleId=$saleId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                "sale_form?saleId={saleId}",
                arguments = listOf(
                    navArgument("saleId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
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

            composable("purchases") {
                PurchasesPlaceholder()
            }

            composable("settings") {
                SettingsPlaceholder()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchasesPlaceholder() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compras") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Compras - Pr\u00f3ximamente",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPlaceholder() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Settings - Pr\u00f3ximamente",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
