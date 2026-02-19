package me.busta.barksaccountant.android.ui.screen.stats

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.stats.StatsMessage
import me.busta.barksaccountant.feature.stats.StatsStore

private val monthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@Composable
fun StatsScreen(serviceLocator: ServiceLocator) {
    val store = remember {
        StatsStore(
            saleRepository = serviceLocator.saleRepository,
            purchaseRepository = serviceLocator.purchaseRepository
        )
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(StatsMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBackground)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            state.error != null -> {
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
                        color = colors.secondaryText
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { store.dispatch(StatsMessage.Started) },
                        colors = ButtonDefaults.buttonColors(containerColor = BarksRed)
                    ) {
                        Text("Reintentar")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { FilterSection(state, store, colors) }
                    item { FinancialSection(state, colors) }
                    item { CountersSection(state, colors) }
                    if (state.selectedMonth == null) {
                        item { MonthlySection(state, colors) }
                    }
                    item { ProductsSection(state, colors) }
                    item { ClientsSection(state, colors) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    state: me.busta.barksaccountant.feature.stats.StatsState,
    store: StatsStore,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    BarksCard(title = "Filtro", colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Year dropdown (segmented picker style on iOS, but we use dropdown)
            if (state.availableYears.isNotEmpty()) {
                var yearExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (state.selectedYear > 0) state.selectedYear.toString() else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Año", style = omnesStyle(15), color = colors.secondaryText) },
                        textStyle = omnesStyle(15, FontWeight.SemiBold).copy(color = colors.primaryText),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BarksRed,
                            unfocusedBorderColor = colors.fieldBorder,
                            focusedContainerColor = colors.fieldBackground,
                            unfocusedContainerColor = colors.fieldBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        state.availableYears.forEach { year ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        year.toString(),
                                        style = omnesStyle(15),
                                        color = colors.primaryText
                                    )
                                },
                                onClick = {
                                    store.dispatch(StatsMessage.YearSelected(year))
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Month dropdown
            var monthExpanded by remember { mutableStateOf(false) }
            val monthLabel = if (state.selectedMonth == null) "Todos" else monthNames[(state.selectedMonth ?: 1) - 1]
            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = { monthExpanded = it }
            ) {
                OutlinedTextField(
                    value = monthLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mes", style = omnesStyle(15), color = colors.secondaryText) },
                    textStyle = omnesStyle(15, FontWeight.SemiBold).copy(color = colors.primaryText),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BarksRed,
                        unfocusedBorderColor = colors.fieldBorder,
                        focusedContainerColor = colors.fieldBackground,
                        unfocusedContainerColor = colors.fieldBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Todos",
                                style = omnesStyle(15),
                                color = colors.primaryText
                            )
                        },
                        onClick = {
                            store.dispatch(StatsMessage.MonthSelected(null))
                            monthExpanded = false
                        }
                    )
                    monthNames.forEachIndexed { index, name ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    name,
                                    style = omnesStyle(15),
                                    color = colors.primaryText
                                )
                            },
                            onClick = {
                                store.dispatch(StatsMessage.MonthSelected(index + 1))
                                monthExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialSection(
    state: me.busta.barksaccountant.feature.stats.StatsState,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    BarksCard(title = "Resumen", colors = colors) {
        if (state.salesCount == 0 && state.totalPurchases == 0.0) {
            EmptyMessage(colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatRow("Ventas totales", formatCurrency(state.totalSales), colors)
                StatRow("Compras totales", formatCurrency(state.totalPurchases), colors)
                StatRow(
                    "Ganancia neta",
                    formatCurrency(state.netProfit),
                    colors,
                    valueColor = if (state.netProfit >= 0) Color(0xFF4CAF50) else BarksRed
                )
                StatRow("Margen", "%.1f%%".format(state.marginPercent), colors)
            }
        }
    }
}

@Composable
private fun CountersSection(
    state: me.busta.barksaccountant.feature.stats.StatsState,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    BarksCard(title = "Indicadores", colors = colors) {
        if (state.salesCount == 0) {
            EmptyMessage(colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatRow("Cantidad de ventas", "${state.salesCount}", colors)
                StatRow("Ticket promedio", formatCurrency(state.averageTicket), colors)
                StatRow("Pendiente de pago", formatCurrency(state.unpaidTotal), colors)
                StatRow("Sin entregar", "${state.undeliveredCount}", colors)
            }
        }
    }
}

@Composable
private fun MonthlySection(
    state: me.busta.barksaccountant.feature.stats.StatsState,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    BarksCard(title = "Desglose mensual", colors = colors) {
        if (state.monthlyBreakdown.isEmpty()) {
            EmptyMessage(colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.monthlyBreakdown.forEach { item ->
                    StatRow(monthNames[item.month - 1], formatCurrency(item.total), colors)
                }
            }
        }
    }
}

@Composable
private fun ProductsSection(
    state: me.busta.barksaccountant.feature.stats.StatsState,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    BarksCard(title = "Productos más vendidos", colors = colors) {
        if (state.topProducts.isEmpty()) {
            EmptyMessage(colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.topProducts.forEach { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = product.name,
                                style = omnesStyle(17, FontWeight.SemiBold),
                                color = colors.primaryText,
                                maxLines = 2
                            )
                            Text(
                                text = "${product.unitsSold} uds",
                                style = omnesStyle(12),
                                color = colors.secondaryText
                            )
                        }
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Text(
                            text = formatCurrency(product.revenue),
                            style = omnesStyle(17, FontWeight.SemiBold),
                            color = colors.primaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientsSection(
    state: me.busta.barksaccountant.feature.stats.StatsState,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    BarksCard(title = "Principales clientes", colors = colors) {
        if (state.topClients.isEmpty()) {
            EmptyMessage(colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.topClients.forEach { client ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = client.name,
                                style = omnesStyle(17, FontWeight.SemiBold),
                                color = colors.primaryText,
                                maxLines = 2
                            )
                            Text(
                                text = "${client.orderCount} pedido${if (client.orderCount == 1) "" else "s"}",
                                style = omnesStyle(12),
                                color = colors.secondaryText
                            )
                        }
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Text(
                            text = formatCurrency(client.totalAmount),
                            style = omnesStyle(17, FontWeight.SemiBold),
                            color = colors.primaryText
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Reusable Components

@Composable
private fun StatRow(
    label: String,
    value: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    valueColor: Color = colors.primaryText
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = omnesStyle(15),
            color = colors.secondaryText
        )
        Spacer(Modifier.padding(horizontal = 6.dp))
        Text(
            text = value,
            style = omnesStyle(15, FontWeight.SemiBold),
            color = valueColor
        )
    }
}

@Composable
private fun EmptyMessage(colors: me.busta.barksaccountant.android.ui.theme.BarksColors) {
    Text(
        text = "Sin datos para mostrar",
        style = omnesStyle(15),
        color = colors.secondaryText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textAlign = TextAlign.Center
    )
}

private fun formatCurrency(value: Double): String = "€%.2f".format(value)
