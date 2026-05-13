package me.busta.barksaccountant.android.ui.screen.purchases

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.screen.sales.ProductPickerDialog
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.purchases.productionplan.ProductionPlanMessage
import me.busta.barksaccountant.feature.purchases.productionplan.ProductionPlanStore
import kotlin.math.truncate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionPlanScreen(
    serviceLocator: ServiceLocator,
    onBack: () -> Unit
) {
    val store = remember {
        ProductionPlanStore(productRepository = serviceLocator.productRepository)
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(ProductionPlanMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    if (state.showProductPicker) {
        val already = state.rows.map { it.productId }.toSet()
        ProductPickerDialog(
            products = state.availableProducts.filter { it.id !in already },
            onSelected = { product -> store.dispatch(ProductionPlanMessage.ProductPicked(product)) },
            onDismiss = { store.dispatch(ProductionPlanMessage.DismissPicker) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Planificar producción",
                        style = omnesStyle(18, FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground,
                    titleContentColor = colors.primaryText,
                    navigationIconContentColor = colors.primaryText
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
            // Helados a producir
            BarksCard(title = "Helados a producir", colors = colors) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.rows.isEmpty()) {
                        Text(
                            text = "No hay helados añadidos",
                            style = omnesStyle(14),
                            color = colors.secondaryText
                        )
                    } else {
                        state.rows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = row.productName,
                                    style = omnesStyle(15, FontWeight.SemiBold),
                                    color = colors.primaryText,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )

                                BasicTextField(
                                    value = row.quantityText,
                                    onValueChange = {
                                        store.dispatch(
                                            ProductionPlanMessage.QuantityChanged(row.productId, it)
                                        )
                                    },
                                    textStyle = omnesStyle(15, FontWeight.SemiBold).copy(
                                        color = colors.primaryText
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.fieldBackground)
                                        .border(
                                            width = 1.dp,
                                            color = colors.fieldBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 10.dp),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (row.quantityText.isEmpty()) {
                                                Text(
                                                    text = "0",
                                                    style = omnesStyle(15, FontWeight.SemiBold),
                                                    color = colors.secondaryText.copy(alpha = 0.5f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                IconButton(
                                    onClick = {
                                        store.dispatch(ProductionPlanMessage.RowRemoved(row.productId))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = BarksRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = BarksRed,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { store.dispatch(ProductionPlanMessage.AddProductTapped) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Añadir helado",
                            style = omnesStyle(14, FontWeight.SemiBold),
                            color = BarksRed
                        )
                    }
                }
            }

            val result = state.result

            if (result.needs.isNotEmpty()) {
                BarksCard(title = "Materia prima necesaria", colors = colors) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.needs.forEach { need ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = need.ingredientName,
                                    style = omnesStyle(15, FontWeight.SemiBold),
                                    color = colors.primaryText,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${formatQty(need.displayQuantity)} ${need.displayUnit.symbol}",
                                    style = omnesStyle(15, FontWeight.SemiBold),
                                    color = colors.primaryText
                                )
                            }
                        }
                    }
                }
            }

            if (result.productsWithoutRecipe.isNotEmpty()) {
                BarksCard(title = "Productos sin receta", colors = colors) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        result.productsWithoutRecipe.forEach { name ->
                            Text(
                                text = name,
                                style = omnesStyle(15),
                                color = colors.secondaryText
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "No se incluyen en el cálculo",
                            style = omnesStyle(12),
                            color = colors.secondaryText
                        )
                    }
                }
            }
        }
    }
}

private fun formatQty(q: Double): String {
    val isWhole = q == truncate(q)
    return if (isWhole) q.toLong().toString()
    else {
        // 2 decimals truncated
        val truncated = truncate(q * 100.0) / 100.0
        val s = truncated.toString()
        // Ensure 2 decimal places
        if (s.contains('.')) {
            val parts = s.split('.')
            val decimals = (parts[1] + "00").take(2)
            "${parts[0]}.$decimals"
        } else "$s.00"
    }
}
