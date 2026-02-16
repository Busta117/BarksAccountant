package me.busta.barksaccountant.android.ui.screen.sales

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.sales.form.SaleFormMessage
import me.busta.barksaccountant.feature.sales.form.SaleFormStore
import me.busta.barksaccountant.model.SaleProduct
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleFormScreen(
    serviceLocator: ServiceLocator,
    saleId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember {
        SaleFormStore(
            saleRepository = serviceLocator.saleRepository,
            productRepository = serviceLocator.productRepository,
            clientRepository = serviceLocator.clientRepository
        )
    }
    val state by store.state.collectAsState()

    var showClientPicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    var showOrderDatePicker by remember { mutableStateOf(false) }
    var showDeliveryDatePicker by remember { mutableStateOf(false) }
    var hasDeliveryDate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        store.dispatch(SaleFormMessage.Started(saleId))
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) {
            onSaved()
        }
    }

    // Update hasDeliveryDate when editing an existing sale
    LaunchedEffect(state.deliveryDate) {
        if (state.deliveryDate != null) {
            hasDeliveryDate = true
        }
    }

    // Client picker dialog
    if (showClientPicker) {
        ClientPickerDialog(
            clients = state.clients,
            onSelected = { name ->
                store.dispatch(SaleFormMessage.ClientSelected(name))
                showClientPicker = false
            },
            onDismiss = { showClientPicker = false }
        )
    }

    // Product picker dialog
    if (showProductPicker) {
        ProductPickerDialog(
            products = state.availableProducts,
            onSelected = { product ->
                store.dispatch(SaleFormMessage.AddProduct(product))
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }

    // Order date picker
    if (showOrderDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showOrderDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        store.dispatch(SaleFormMessage.OrderDateChanged(formatMillisToDate(millis)))
                    }
                    showOrderDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOrderDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delivery date picker
    if (showDeliveryDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDeliveryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        store.dispatch(SaleFormMessage.DeliveryDateChanged(formatMillisToDate(millis)))
                    }
                    showDeliveryDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditing) "Editar Venta" else "Nueva Venta")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Client section
            Text(
                "Cliente",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showClientPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.clientName.ifEmpty { "Seleccionar cliente" },
                    color = if (state.clientName.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            // Responsible section
            Text(
                "Responsable (opcional)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.responsible,
                onValueChange = { store.dispatch(SaleFormMessage.ResponsibleChanged(it)) },
                label = { Text("Responsable") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Dates section
            Text(
                "Fechas",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showOrderDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fecha de pedido: ${state.orderDate}")
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fecha de entrega", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = hasDeliveryDate,
                    onCheckedChange = { checked ->
                        hasDeliveryDate = checked
                        if (!checked) {
                            store.dispatch(SaleFormMessage.DeliveryDateChanged(null))
                        }
                    }
                )
            }

            if (hasDeliveryDate) {
                OutlinedButton(
                    onClick = { showDeliveryDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrega: ${state.deliveryDate ?: "Seleccionar"}")
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Products section
            Text(
                "Productos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            state.products.forEachIndexed { index, product ->
                ProductFormRow(
                    product = product,
                    onIncrement = { store.dispatch(SaleFormMessage.IncrementQuantity(index)) },
                    onDecrement = { store.dispatch(SaleFormMessage.DecrementQuantity(index)) }
                )
                if (index < state.products.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { showProductPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Agregar Producto")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Total section
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "\u20ac%.2f".format(state.totalPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // Save button
            Button(
                onClick = { store.dispatch(SaleFormMessage.SaveTapped) },
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar", fontWeight = FontWeight.SemiBold)
                }
            }

            state.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ProductFormRow(
    product: SaleProduct,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "\u20ac%.2f".format(product.unitPrice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onDecrement) {
            Icon(
                imageVector = if (product.quantity <= 1) Icons.Default.Delete else Icons.Default.RemoveCircle,
                contentDescription = if (product.quantity <= 1) "Eliminar" else "Reducir",
                tint = if (product.quantity <= 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "${product.quantity}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        IconButton(onClick = onIncrement) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Aumentar",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "\u20ac%.2f".format(product.totalPrice),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(72.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}
