package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksFab
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.android.ui.theme.vagRundschriftStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.products.list.ProductsListMessage
import me.busta.barksaccountant.feature.settings.products.list.ProductsListStore
import me.busta.barksaccountant.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreen(
    serviceLocator: ServiceLocator,
    onProductClick: (String) -> Unit,
    onNewProduct: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember { ProductsListStore(productRepository = serviceLocator.productRepository) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) { store.dispatch(ProductsListMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    val colors = barksColors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground,
                    titleContentColor = colors.primaryText,
                    navigationIconContentColor = colors.primaryText
                )
            )
        },
        containerColor = colors.screenBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.products.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null && state.products.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error ?: "",
                            style = omnesStyle(17),
                            color = colors.primaryText.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = { store.dispatch(ProductsListMessage.Started) }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                state.products.isEmpty() -> {
                    Text(
                        text = "No hay productos",
                        style = vagRundschriftStyle(20),
                        color = colors.primaryText.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            ProductCardRow(
                                product = product,
                                colors = colors,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
            }

            // FAB bottom-right
            if (state.products.isNotEmpty() || (!state.isLoading && state.error == null)) {
                BarksFab(
                    onClick = onNewProduct,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.35f else 0.18f),
                            spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.35f else 0.18f)
                        ),
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun ProductCardRow(
    product: Product,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.22f else 0.08f),
                spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.22f else 0.08f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBackground)
            .then(
                if (colors.isDark) {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.06f),
                        RoundedCornerShape(18.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Product name - left aligned, can wrap to 2 lines
        Text(
            text = product.name,
            style = omnesStyle(17, FontWeight.SemiBold),
            color = colors.primaryText,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 12.dp),
            maxLines = 2
        )

        // Price - right aligned, monospaced digits
        Text(
            text = String.format("€%.2f", product.unitPrice),
            style = omnesStyle(17, FontWeight.SemiBold).copy(
                fontFeatureSettings = "tnum" // tabular numbers (monospaced digits)
            ),
            color = colors.primaryText
        )
    }
}
