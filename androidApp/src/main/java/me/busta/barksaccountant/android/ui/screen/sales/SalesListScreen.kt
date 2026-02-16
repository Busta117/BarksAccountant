package me.busta.barksaccountant.android.ui.screen.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(Unit) {
        store.dispatch(SalesListMessage.Started)
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas") },
                actions = {
                    IconButton(onClick = onNewSale) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva venta")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.sales.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { store.dispatch(SalesListMessage.Started) }) {
                        Text("Reintentar")
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(state.sales, key = { it.id }) { sale ->
                        SaleRow(sale = sale, onClick = { onSaleClick(sale.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleRow(sale: Sale, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
    ) {
        if (!sale.isPaid) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color.Red)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = if (sale.isPaid) 16.dp else 12.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
        ) {
            Text(
                text = sale.clientName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    text = sale.orderDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "\u20ac%.2f".format(sale.totalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
