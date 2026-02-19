package me.busta.barksaccountant.android.ui.screen.sales

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.R
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
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
    onBack: () -> Unit,
    onInvoiceReady: (html: String, saleId: String) -> Unit,
    onSummaryReady: (html: String, saleId: String) -> Unit
) {
    val store = remember {
        SaleDetailStore(
            saleRepository = serviceLocator.saleRepository,
            clientRepository = serviceLocator.clientRepository,
            businessInfoRepository = serviceLocator.businessInfoRepository
        )
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) {
        store.dispatch(SaleDetailMessage.Started(saleId))
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    LaunchedEffect(state.invoiceHtml) {
        val html = state.invoiceHtml
        if (html != null) {
            onInvoiceReady(html, saleId)
            store.dispatch(SaleDetailMessage.InvoiceDismissed)
        }
    }

    LaunchedEffect(state.summaryHtml) {
        val html = state.summaryHtml
        if (html != null) {
            onSummaryReady(html, saleId)
            store.dispatch(SaleDetailMessage.SummaryDismissed)
        }
    }

    // Pay confirmation dialog
    if (state.showPayConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(SaleDetailMessage.DismissConfirm) },
            title = { Text("Marcar como pagado") },
            text = { Text("¿Desea marcar esta venta como pagada?") },
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
            text = { Text("¿Desea marcar esta venta como entregada?") },
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
        containerColor = colors.screenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de Venta",
                        style = omnesStyle(17, FontWeight.SemiBold),
                        color = colors.primaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.primaryText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditSale) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground
                )
            )
        }
    ) { padding ->
        when {
            state.sale != null -> {
                SaleContent(
                    sale = state.sale!!,
                    colors = colors,
                    isGeneratingInvoice = state.isGeneratingInvoice,
                    isGeneratingSummary = state.isGeneratingSummary,
                    modifier = Modifier.padding(padding),
                    onMarkAsPaid = { store.dispatch(SaleDetailMessage.MarkAsPaidTapped) },
                    onMarkAsDelivered = { store.dispatch(SaleDetailMessage.MarkAsDeliveredTapped) },
                    onExport = { store.dispatch(SaleDetailMessage.ExportTapped) },
                    onShareSummary = { store.dispatch(SaleDetailMessage.ShareSummaryTapped) }
                )
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(colors.screenBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.error ?: "",
                            style = omnesStyle(17),
                            color = colors.secondaryText
                        )
                        Button(
                            onClick = { store.dispatch(SaleDetailMessage.Started(saleId)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BarksRed
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(colors.screenBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BarksRed)
                }
            }
        }
    }
}

@Composable
private fun SaleContent(
    sale: Sale,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    isGeneratingInvoice: Boolean,
    isGeneratingSummary: Boolean,
    modifier: Modifier = Modifier,
    onMarkAsPaid: () -> Unit,
    onMarkAsDelivered: () -> Unit,
    onExport: () -> Unit,
    onShareSummary: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cliente card
        BarksCard(title = "Cliente", colors = colors) {
            InfoRow("Nombre", sale.clientName, colors)
        }

        // Productos card
        BarksCard(title = "Productos", colors = colors) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sale.products.forEachIndexed { index, product ->
                    ProductRow(product, colors)
                    if (index != sale.products.lastIndex) {
                        HorizontalDivider(
                            color = colors.primaryText.copy(alpha = if (colors.isDark) 0.25f else 0.18f)
                        )
                    }
                }

                HorizontalDivider(
                    color = colors.primaryText.copy(alpha = if (colors.isDark) 0.25f else 0.18f)
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Total",
                        style = omnesStyle(18, FontWeight.Bold),
                        color = colors.primaryText
                    )
                    Text(
                        text = String.format("€%.2f", sale.totalPrice),
                        style = omnesStyle(22, FontWeight.Bold),
                        color = colors.primaryText
                    )
                }
            }
        }

        // Fechas card
        BarksCard(title = "Fechas", colors = colors) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                InfoRow("Fecha de pedido", sale.orderDate, colors)
                InfoRow(
                    "Fecha de entrega",
                    sale.deliveryDate ?: "Sin entregar",
                    colors
                )
            }
        }

        // Estado card
        BarksCard(title = "Estado", colors = colors) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                StatusRow("Pagado", sale.isPaid, colors)
                StatusRow("Entregado", sale.isDelivered, colors)
            }
        }

        // Action buttons card
        BarksCard(colors = colors) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!sale.isPaid) {
                    PrimaryActionButton(
                        text = "Marcar como pagado",
                        onClick = onMarkAsPaid
                    )
                }

                if (!sale.isDelivered) {
                    PrimaryActionButton(
                        text = "Marcar como entregado",
                        onClick = onMarkAsDelivered
                    )
                }

                SecondaryActionButton(
                    text = if (isGeneratingInvoice) "Generando..." else "Ver factura",
                    onClick = onExport,
                    enabled = !isGeneratingInvoice
                )

                SecondaryActionButton(
                    text = if (isGeneratingSummary) "Generando..." else "Compartir resumen",
                    onClick = onShareSummary,
                    enabled = !isGeneratingSummary
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = omnesStyle(15),
            color = colors.secondaryText,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = value,
            style = omnesStyle(15, FontWeight.SemiBold),
            color = colors.primaryText,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProductRow(
    product: SaleProduct,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
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
                maxLines = 1
            )
            Text(
                text = String.format("€%.2f x %d", product.unitPrice, product.quantity),
                style = omnesStyle(13),
                color = colors.secondaryText,
                maxLines = 1
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = String.format("€%.2f", product.totalPrice),
            style = omnesStyle(17, FontWeight.SemiBold),
            color = colors.primaryText
        )
    }
}

@Composable
private fun StatusRow(
    label: String,
    isOn: Boolean,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = omnesStyle(15),
            color = colors.secondaryText
        )
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = if (isOn) "Sí" else "No",
            tint = if (isOn) Color(0xFF4CAF50) else BarksRed,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BarksRed,
            contentColor = BarksWhite,
            disabledContainerColor = BarksRed.copy(alpha = 0.6f),
            disabledContentColor = BarksWhite.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            style = omnesStyle(16, FontWeight.SemiBold)
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = barksColors().accentColor
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) tint else tint.copy(alpha = 0.4f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = tint,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = tint.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = text,
            style = omnesStyle(16, FontWeight.SemiBold)
        )
    }
}
