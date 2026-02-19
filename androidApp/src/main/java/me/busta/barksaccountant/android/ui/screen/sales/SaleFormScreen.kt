package me.busta.barksaccountant.android.ui.screen.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
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
    personName: String,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember {
        SaleFormStore(
            saleRepository = serviceLocator.saleRepository,
            productRepository = serviceLocator.productRepository,
            clientRepository = serviceLocator.clientRepository,
            createdBy = personName
        )
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

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

    LaunchedEffect(state.deletedSuccessfully) {
        if (state.deletedSuccessfully) {
            onSaved()
        }
    }

    // Update hasDeliveryDate when editing an existing sale
    LaunchedEffect(state.deliveryDate) {
        if (state.deliveryDate != null) {
            hasDeliveryDate = true
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(SaleFormMessage.DismissDelete) },
            title = { Text("Eliminar Venta") },
            text = { Text("¿Estás seguro de que quieres eliminar esta venta?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(SaleFormMessage.ConfirmDelete) }) {
                    Text("Eliminar", color = BarksRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(SaleFormMessage.DismissDelete) }) {
                    Text("Cancelar")
                }
            }
        )
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
                    Text(
                        if (state.isEditing) "Editar Venta" else "Nueva Venta",
                        style = omnesStyle(18, FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Volver",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground
                )
            )
        },
        containerColor = colors.screenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Client Card
            ClientCard(
                clientName = state.clientName,
                colors = colors,
                onClick = { showClientPicker = true }
            )

            // Dates Card
            DatesCard(
                orderDate = state.orderDate,
                deliveryDate = state.deliveryDate,
                hasDeliveryDate = hasDeliveryDate,
                colors = colors,
                onOrderDateClick = { showOrderDatePicker = true },
                onDeliveryDateClick = { showDeliveryDatePicker = true },
                onDeliveryToggle = { checked ->
                    hasDeliveryDate = checked
                    if (!checked) {
                        store.dispatch(SaleFormMessage.DeliveryDateChanged(null))
                    }
                }
            )

            // Products Card
            ProductsCard(
                products = state.products,
                colors = colors,
                onAddProduct = { showProductPicker = true },
                onIncrement = { index -> store.dispatch(SaleFormMessage.IncrementQuantity(index)) },
                onDecrement = { index -> store.dispatch(SaleFormMessage.DecrementQuantity(index)) }
            )

            // Total Card
            TotalCard(
                totalPrice = state.totalPrice,
                colors = colors
            )

            // Save Card
            SaveCard(
                canSave = state.canSave,
                isSaving = state.isSaving,
                colors = colors,
                onSave = { store.dispatch(SaleFormMessage.SaveTapped) }
            )

            // Delete Card (only in edit mode)
            if (state.isEditing) {
                DeleteCard(
                    colors = colors,
                    onDelete = { store.dispatch(SaleFormMessage.DeleteTapped) }
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    style = omnesStyle(13),
                    color = BarksRed,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ─── Client Card ─────────────────────────────────────────────────────────────

@Composable
private fun ClientCard(
    clientName: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onClick: () -> Unit
) {
    BarksCard(title = "Cliente", colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clientName.ifEmpty { "Seleccionar cliente" },
                    style = omnesStyle(17, FontWeight.SemiBold),
                    color = if (clientName.isEmpty()) colors.secondaryText else colors.primaryText
                )
                if (clientName.isEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Requerido",
                        style = omnesStyle(13),
                        color = BarksRed.copy(alpha = if (colors.isDark) 0.9f else 1.0f)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.secondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Dates Card ──────────────────────────────────────────────────────────────

@Composable
private fun DatesCard(
    orderDate: String,
    deliveryDate: String?,
    hasDeliveryDate: Boolean,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onOrderDateClick: () -> Unit,
    onDeliveryDateClick: () -> Unit,
    onDeliveryToggle: (Boolean) -> Unit
) {
    BarksCard(title = "Fechas", colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Order Date
            DatePickerButton(
                label = "Fecha de pedido",
                date = orderDate,
                colors = colors,
                onClick = onOrderDateClick
            )

            // Delivery Date Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fecha de entrega",
                    style = omnesStyle(17, FontWeight.SemiBold),
                    color = colors.primaryText
                )
                Switch(
                    checked = hasDeliveryDate,
                    onCheckedChange = onDeliveryToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BarksWhite,
                        checkedTrackColor = BarksRed,
                        uncheckedThumbColor = colors.secondaryText,
                        uncheckedTrackColor = colors.secondaryText.copy(alpha = 0.3f)
                    )
                )
            }

            // Delivery Date Picker
            if (hasDeliveryDate) {
                DatePickerButton(
                    label = "Entrega",
                    date = deliveryDate ?: "Seleccionar",
                    colors = colors,
                    onClick = onDeliveryDateClick
                )
            }
        }
    }
}

@Composable
private fun DatePickerButton(
    label: String,
    date: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = omnesStyle(17, FontWeight.SemiBold),
            color = colors.primaryText
        )
        Text(
            text = date,
            style = omnesStyle(15),
            color = colors.secondaryText
        )
    }
}

// ─── Products Card ───────────────────────────────────────────────────────────

@Composable
private fun ProductsCard(
    products: List<SaleProduct>,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onAddProduct: () -> Unit,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit
) {
    BarksCard(title = "Productos", colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (products.isEmpty()) {
                Text(
                    text = "Agrega al menos un producto",
                    style = omnesStyle(15),
                    color = colors.secondaryText
                )
            } else {
                products.forEachIndexed { index, product ->
                    ProductRow(
                        product = product,
                        colors = colors,
                        onIncrement = { onIncrement(index) },
                        onDecrement = { onDecrement(index) }
                    )
                    if (index < products.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = colors.primaryText.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Add Product Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddProduct)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = BarksRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Agregar producto",
                    style = omnesStyle(16, FontWeight.SemiBold),
                    color = colors.primaryText
                )
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: SaleProduct,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Product Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = product.name,
                style = omnesStyle(17, FontWeight.SemiBold),
                color = colors.primaryText,
                maxLines = 2
            )
            Text(
                text = "€%.2f".format(product.unitPrice),
                style = omnesStyle(13),
                color = colors.secondaryText
            )
        }

        Spacer(Modifier.width(8.dp))

        // Quantity Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement / Delete
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (product.quantity <= 1) Icons.Default.Delete else Icons.Default.Remove,
                    contentDescription = if (product.quantity <= 1) "Eliminar" else "Reducir",
                    tint = if (product.quantity <= 1) BarksRed else BarksLightBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Quantity
            Text(
                text = "${product.quantity}",
                style = omnesStyle(17, FontWeight.SemiBold),
                color = colors.primaryText,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )

            // Increment
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar",
                    tint = BarksLightBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Total Price
            Text(
                text = "€%.2f".format(product.totalPrice),
                style = omnesStyle(17, FontWeight.SemiBold),
                color = colors.primaryText,
                modifier = Modifier.width(84.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─── Total Card ──────────────────────────────────────────────────────────────

@Composable
private fun TotalCard(
    totalPrice: Double,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    BarksCard(colors = colors) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                style = omnesStyle(18, FontWeight.Bold),
                color = colors.primaryText
            )
            Text(
                text = "€%.2f".format(totalPrice),
                style = omnesStyle(22, FontWeight.Bold),
                color = colors.primaryText
            )
        }
    }
}

// ─── Save Card ───────────────────────────────────────────────────────────────

@Composable
private fun SaveCard(
    canSave: Boolean,
    isSaving: Boolean,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onSave: () -> Unit
) {
    BarksCard(colors = colors) {
        Button(
            onClick = onSave,
            enabled = canSave && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BarksRed,
                contentColor = BarksWhite,
                disabledContainerColor = BarksRed.copy(alpha = 0.5f),
                disabledContentColor = BarksWhite.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BarksWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Guardar",
                    style = omnesStyle(16, FontWeight.SemiBold)
                )
            }
        }
    }
}

// ─── Delete Card ─────────────────────────────────────────────────────────────

@Composable
private fun DeleteCard(
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onDelete: () -> Unit
) {
    BarksCard(colors = colors) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.5.dp,
                    color = BarksRed,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Eliminar venta",
                style = omnesStyle(16, FontWeight.SemiBold),
                color = BarksRed
            )
        }
    }
}

// ─── Helper Functions ────────────────────────────────────────────────────────

private fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}
