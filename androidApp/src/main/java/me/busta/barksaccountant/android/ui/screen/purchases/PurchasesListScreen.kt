package me.busta.barksaccountant.android.ui.screen.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import me.busta.barksaccountant.android.ui.theme.BarksBlack
import me.busta.barksaccountant.android.ui.theme.BarksFab
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.android.ui.theme.vagRundschriftStyle
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
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(PurchasesListMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compras", style = omnesStyle(17, FontWeight.SemiBold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground,
                    titleContentColor = colors.primaryText
                )
            )
        },
        floatingActionButton = {
            BarksFab(onClick = onNewPurchase, colors = colors)
        },
        containerColor = colors.screenBackground
    ) { padding ->
        when {
            state.isLoading && state.purchases.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BarksRed)
                }
            }
            state.error != null && state.purchases.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        state.error ?: "",
                        style = omnesStyle(17),
                        color = colors.primaryText.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { store.dispatch(PurchasesListMessage.Started) }
                    ) {
                        Text("Retry")
                    }
                }
            }
            state.purchases.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No purchases yet",
                        style = vagRundschriftStyle(20),
                        color = colors.primaryText.copy(alpha = 0.7f)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp // Extra space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.purchases, key = { it.id }) { purchase ->
                        PurchaseCardRow(
                            purchase = purchase,
                            colors = colors,
                            onClick = { onPurchaseClick(purchase.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseCardRow(
    purchase: Purchase,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onClick: () -> Unit
) {
    val shadowColor = if (colors.isDark) {
        Color.Black.copy(alpha = 0.22f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

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
            .then(
                if (colors.isDark) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(18.dp)
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title
            Text(
                text = purchase.title,
                style = omnesStyle(17, FontWeight.SemiBold),
                color = colors.primaryText,
                maxLines = 1
            )

            // Description (if present)
            purchase.description?.let { desc ->
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = omnesStyle(15),
                        color = colors.secondaryText
                    )
                }
            }

            // Calendar icon + date
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = BarksLightBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = purchase.date,
                    style = omnesStyle(15),
                    color = colors.secondaryText,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        // Price (right-aligned)
        Text(
            text = String.format("€%.2f", purchase.value),
            style = omnesStyle(17, FontWeight.SemiBold),
            color = colors.primaryText
        )
    }
}
