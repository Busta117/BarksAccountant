package me.busta.barksaccountant.android.ui.screen.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksFab
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.android.ui.theme.vagRundschriftStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.sales.list.SalesListMessage
import me.busta.barksaccountant.feature.sales.list.SalesListStore
import me.busta.barksaccountant.model.Sale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesListScreen(
    serviceLocator: ServiceLocator,
    onSaleClick: (String) -> Unit,
    onNewSale: () -> Unit
) {
    val store = remember { SalesListStore(saleRepository = serviceLocator.saleRepository) }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) {
        store.dispatch(SalesListMessage.Started)
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ventas",
                        style = omnesStyle(20, FontWeight.SemiBold),
                        color = colors.primaryText
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground
                )
            )
        },
        floatingActionButton = {
            BarksFab(onClick = onNewSale)
        },
        containerColor = colors.screenBackground
    ) { padding ->
        when {
            state.isLoading && state.sales.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BarksRed)
                }
            }
            state.error != null && state.sales.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        state.error ?: "",
                        style = omnesStyle(17),
                        color = colors.primaryText.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { store.dispatch(SalesListMessage.Started) }) {
                        Text(
                            "Reintentar",
                            style = omnesStyle(15, FontWeight.SemiBold),
                            color = BarksRed
                        )
                    }
                }
            }
            state.sales.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay ventas",
                        style = vagRundschriftStyle(20),
                        color = colors.primaryText.copy(alpha = 0.7f)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.screenBackground)
                        .padding(padding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    items(state.sales, key = { it.id }) { sale ->
                        SaleCardRow(
                            sale = sale,
                            onClick = { onSaleClick(sale.id) },
                            colors = colors
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleCardRow(
    sale: Sale,
    onClick: () -> Unit,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    val shadowColor = Color.Black.copy(alpha = if (colors.isDark) 0.22f else 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Red left border for unpaid sales
        if (!sale.isPaid) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp) // approximate height to match card content
                    .clip(RoundedCornerShape(2.dp))
                    .background(BarksRed)
            )
            Spacer(Modifier.width(12.dp))
        }

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Client name
            Text(
                text = sale.clientName,
                style = omnesStyle(17, FontWeight.SemiBold),
                color = colors.primaryText
            )

            Spacer(Modifier.height(6.dp))

            // Date with calendar icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = BarksLightBlue,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = sale.orderDate,
                    style = omnesStyle(15),
                    color = colors.secondaryText
                )
            }

            Spacer(Modifier.height(6.dp))

            // Status text
            Text(
                text = if (sale.isPaid) "Pagada" else "Pendiente",
                style = omnesStyle(13, FontWeight.SemiBold),
                color = if (sale.isPaid) {
                    colors.secondaryText
                } else {
                    BarksRed.copy(alpha = if (colors.isDark) 0.9f else 1.0f)
                }
            )
        }

        Spacer(Modifier.width(12.dp))

        // Price (right-aligned)
        Text(
            text = "€%.2f".format(sale.totalPrice),
            style = omnesStyle(17, FontWeight.SemiBold),
            color = colors.primaryText
        )
    }
}
