package me.busta.barksaccountant.android.ui.screen.stats

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.stats.StatsMessage
import me.busta.barksaccountant.feature.stats.StatsStore

private val monthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(serviceLocator: ServiceLocator) {
    val store = remember {
        StatsStore(
            saleRepository = serviceLocator.saleRepository,
            purchaseRepository = serviceLocator.purchaseRepository
        )
    }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) { store.dispatch(StatsMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stats") }) }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            state.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { store.dispatch(StatsMessage.Started) }) {
                        Text("Reintentar")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item { FilterSection(state, store) }
                    item { FinancialSection(state) }
                    item { CountersSection(state) }
                    if (state.selectedMonth == null) {
                        item { MonthlySection(state) }
                    }
                    item { ProductsSection(state) }
                    item { ClientsSection(state) }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    state: me.busta.barksaccountant.feature.stats.StatsState,
    store: StatsStore
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Year dropdown
        var yearExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = yearExpanded,
            onExpandedChange = { yearExpanded = it }
        ) {
            OutlinedTextField(
                value = if (state.selectedYear > 0) state.selectedYear.toString() else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Año") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                state.availableYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            store.dispatch(StatsMessage.YearSelected(year))
                            yearExpanded = false
                        }
                    )
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
                label = { Text("Mes") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Todos") },
                    onClick = {
                        store.dispatch(StatsMessage.MonthSelected(null))
                        monthExpanded = false
                    }
                )
                monthNames.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        text = { Text(name) },
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

@Composable
private fun FinancialSection(state: me.busta.barksaccountant.feature.stats.StatsState) {
    SectionCard(title = "Resumen") {
        if (state.salesCount == 0 && state.totalPurchases == 0.0) {
            EmptyMessage()
        } else {
            StatRow("Ventas totales", formatCurrency(state.totalSales))
            StatRow("Compras totales", formatCurrency(state.totalPurchases))
            StatRow(
                "Ganancia neta",
                formatCurrency(state.netProfit),
                valueColor = if (state.netProfit >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
            StatRow("Margen", "%.1f%%".format(state.marginPercent))
        }
    }
}

@Composable
private fun CountersSection(state: me.busta.barksaccountant.feature.stats.StatsState) {
    SectionCard(title = "Indicadores") {
        if (state.salesCount == 0) {
            EmptyMessage()
        } else {
            StatRow("Cantidad de ventas", "${state.salesCount}")
            StatRow("Ticket promedio", formatCurrency(state.averageTicket))
            StatRow("Pendiente de pago", formatCurrency(state.unpaidTotal))
            StatRow("Sin entregar", "${state.undeliveredCount}")
        }
    }
}

@Composable
private fun MonthlySection(state: me.busta.barksaccountant.feature.stats.StatsState) {
    SectionCard(title = "Desglose mensual") {
        if (state.monthlyBreakdown.isEmpty()) {
            EmptyMessage()
        } else {
            state.monthlyBreakdown.forEach { item ->
                StatRow(monthNames[item.month - 1], formatCurrency(item.total))
            }
        }
    }
}

@Composable
private fun ProductsSection(state: me.busta.barksaccountant.feature.stats.StatsState) {
    SectionCard(title = "Productos más vendidos") {
        if (state.topProducts.isEmpty()) {
            EmptyMessage()
        } else {
            state.topProducts.forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${product.unitsSold} uds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        formatCurrency(product.revenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientsSection(state: me.busta.barksaccountant.feature.stats.StatsState) {
    SectionCard(title = "Principales clientes") {
        if (state.topClients.isEmpty()) {
            EmptyMessage()
        } else {
            state.topClients.forEach { client ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(client.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${client.orderCount} pedido${if (client.orderCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        formatCurrency(client.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// MARK: - Reusable Components

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun EmptyMessage() {
    Text(
        "Sin datos para mostrar",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    )
}

private fun formatCurrency(value: Double): String = "\u20ac%.2f".format(value)
