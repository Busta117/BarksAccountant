package me.busta.barksaccountant.android.ui.screen.purchases

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.purchases.list.PurchasesListMessage
import me.busta.barksaccountant.feature.purchases.list.PurchasesListStore
import me.busta.barksaccountant.model.Purchase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesListScreen(
    serviceLocator: ServiceLocator,
    onPurchaseClick: (String) -> Unit,
    onNewPurchase: () -> Unit
) {
    val store = remember { PurchasesListStore(purchaseRepository = serviceLocator.purchaseRepository) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) { store.dispatch(PurchasesListMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compras") },
                actions = {
                    IconButton(onClick = onNewPurchase) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva compra")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.purchases.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            state.error != null && state.purchases.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { store.dispatch(PurchasesListMessage.Started) }) {
                        Text("Reintentar")
                    }
                }
            }
            state.purchases.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay compras",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    items(state.purchases, key = { it.id }) { purchase ->
                        PurchaseRow(purchase = purchase, onClick = { onPurchaseClick(purchase.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseRow(purchase: Purchase, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = purchase.title, style = MaterialTheme.typography.titleMedium)
        purchase.description?.let { desc ->
            if (desc.isNotEmpty()) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row {
            Text(
                text = purchase.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "\u20ac%.2f".format(purchase.value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
