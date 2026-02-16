package me.busta.barksaccountant.android.ui.screen.sales

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import me.busta.barksaccountant.feature.sales.detail.SaleDetailMessage
import me.busta.barksaccountant.feature.sales.detail.SaleDetailStore
import me.busta.barksaccountant.model.Sale
import me.busta.barksaccountant.model.SaleProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(
    serviceLocator: ServiceLocator,
    saleId: String,
    onEditSale: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember { SaleDetailStore(saleRepository = serviceLocator.saleRepository) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) {
        store.dispatch(SaleDetailMessage.Started(saleId))
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    // Pay confirmation dialog
    if (state.showPayConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(SaleDetailMessage.DismissConfirm) },
            title = { Text("Marcar como pagado") },
            text = { Text("\u00bfDesea marcar esta venta como pagada?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(SaleDetailMessage.ConfirmPaid) }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(SaleDetailMessage.DismissConfirm) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Deliver confirmation dialog
    if (state.showDeliverConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(SaleDetailMessage.DismissConfirm) },
            title = { Text("Marcar como entregado") },
            text = { Text("\u00bfDesea marcar esta venta como entregada?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(SaleDetailMessage.ConfirmDelivered) }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(SaleDetailMessage.DismissConfirm) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Venta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditSale) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.sale != null -> {
                SaleContent(
                    sale = state.sale!!,
                    modifier = Modifier.padding(padding),
                    onMarkAsPaid = { store.dispatch(SaleDetailMessage.MarkAsPaidTapped) },
                    onMarkAsDelivered = { store.dispatch(SaleDetailMessage.MarkAsDeliveredTapped) }
                )
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.error ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun SaleContent(
    sale: Sale,
    modifier: Modifier = Modifier,
    onMarkAsPaid: () -> Unit,
    onMarkAsDelivered: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Client section
        SectionHeader("Cliente")
        LabeledRow("Nombre", sale.clientName)
        sale.responsible?.let { LabeledRow("Responsable", it) }
        if (sale.createdBy.isNotEmpty()) {
            LabeledRow("Creado por", sale.createdBy)
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // Products section
        SectionHeader("Productos")
        Spacer(Modifier.height(8.dp))
        sale.products.forEach { product ->
            ProductRow(product)
            Spacer(Modifier.height(8.dp))
        }
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text(
                "\u20ac%.2f".format(sale.totalPrice),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // Dates section
        SectionHeader("Fechas")
        LabeledRow("Fecha de pedido", sale.orderDate)
        LabeledRow("Fecha de entrega", sale.deliveryDate ?: "Sin entregar")

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // Status section
        SectionHeader("Estado")
        Spacer(Modifier.height(8.dp))
        StatusRow("Pagado", sale.isPaid)
        StatusRow("Entregado", sale.isDelivered)

        Spacer(Modifier.height(24.dp))

        // Action buttons
        if (!sale.isPaid) {
            Button(
                onClick = onMarkAsPaid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Marcar como pagado")
            }
            Spacer(Modifier.height(8.dp))
        }

        if (!sale.isDelivered) {
            Button(
                onClick = onMarkAsDelivered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Marcar como entregado")
            }
            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { /* TODO: Export */ },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exportar")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProductRow(product: SaleProduct) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "\u20ac%.2f x %d".format(product.unitPrice, product.quantity),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "\u20ac%.2f".format(product.totalPrice),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusRow(label: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = if (isActive) "S\u00ed" else "No",
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
