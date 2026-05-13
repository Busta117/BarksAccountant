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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.screen.sales.ProductPickerDialog
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.purchases.productioncapacity.ProductionCapacityMessage
import me.busta.barksaccountant.feature.purchases.productioncapacity.ProductionCapacityStore
import me.busta.barksaccountant.model.ProductIngredient
import kotlin.math.truncate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionCapacityScreen(
    serviceLocator: ServiceLocator,
    onBack: () -> Unit
) {
    val store = remember {
        ProductionCapacityStore(productRepository = serviceLocator.productRepository)
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(ProductionCapacityMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    if (state.showProductPicker) {
        ProductPickerDialog(
            products = state.availableProducts,
            onSelected = { p -> store.dispatch(ProductionCapacityMessage.ProductPicked(p)) },
            onDismiss = { store.dispatch(ProductionCapacityMessage.DismissProductPicker) }
        )
    }

    if (state.showIngredientPicker) {
        val productIngs = state.selectedProduct?.ingredients ?: emptyList()
        AlertDialog(
            onDismissRequest = { store.dispatch(ProductionCapacityMessage.DismissIngredientPicker) },
            title = {
                Text(
                    "Elegir ingrediente",
                    style = omnesStyle(17, FontWeight.SemiBold),
                    color = colors.primaryText
                )
            },
            text = {
                if (productIngs.isEmpty()) {
                    Text(
                        "Este helado no tiene ingredientes",
                        style = omnesStyle(15),
                        color = colors.secondaryText
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(productIngs, key = { it.ingredientId }) { ing ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        store.dispatch(
                                            ProductionCapacityMessage.IngredientPicked(ing)
                                        )
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ing.ingredientName,
                                    style = omnesStyle(16, FontWeight.SemiBold),
                                    color = colors.primaryText
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BarksLightBlue.copy(alpha = 0.35f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = ing.unit.symbol,
                                        style = omnesStyle(13, FontWeight.SemiBold),
                                        color = colors.primaryText
                                    )
                                }
                            }
                            HorizontalDivider(
                                color = colors.primaryText.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    store.dispatch(ProductionCapacityMessage.DismissIngredientPicker)
                }) {
                    Text(
                        "Cerrar",
                        style = omnesStyle(15, FontWeight.SemiBold)
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "¿Qué puedo producir?",
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
            // Step 1: Product
            BarksCard(title = "1. Helado", colors = colors) {
                PickerBox(
                    text = state.selectedProduct?.name ?: "Selecciona un helado",
                    placeholder = state.selectedProduct == null,
                    enabled = true,
                    colors = colors,
                    onClick = { store.dispatch(ProductionCapacityMessage.ProductPickerOpened) }
                )
            }

            // Step 2: Ingredient
            BarksCard(title = "2. Ingrediente", colors = colors) {
                val product = state.selectedProduct
                val hasRecipe = product != null && product.ingredients.isNotEmpty()
                val enabled = hasRecipe
                val ing = state.selectedIngredient
                val label = when {
                    product == null -> "Selecciona primero un helado"
                    product.ingredients.isEmpty() -> "Este helado no tiene receta definida"
                    ing == null -> "Elige un ingrediente"
                    else -> "${ing.ingredientName} (${ing.unit.symbol})"
                }
                PickerBox(
                    text = label,
                    placeholder = ing == null,
                    enabled = enabled,
                    colors = colors,
                    onClick = { store.dispatch(ProductionCapacityMessage.IngredientPickerOpened) }
                )
            }

            // Step 3: Available quantity
            BarksCard(title = "3. Tengo", colors = colors) {
                val ing = state.selectedIngredient
                val enabled = ing != null

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BasicTextField(
                        value = state.availableQuantityText,
                        onValueChange = {
                            store.dispatch(ProductionCapacityMessage.AvailableQuantityChanged(it))
                        },
                        textStyle = omnesStyle(17, FontWeight.SemiBold).copy(
                            color = if (enabled) colors.primaryText else colors.primaryText.copy(alpha = 0.5f)
                        ),
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.fieldBackground)
                            .border(
                                width = 1.dp,
                                color = colors.fieldBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (state.availableQuantityText.isEmpty()) {
                                    Text(
                                        text = "0",
                                        style = omnesStyle(17, FontWeight.SemiBold),
                                        color = colors.secondaryText.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Text(
                        text = ing?.unit?.symbol ?: "",
                        style = omnesStyle(16, FontWeight.SemiBold),
                        color = colors.secondaryText,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }

            // Result
            val result = state.result
            val selectedProduct = state.selectedProduct
            if (result != null && selectedProduct != null) {
                BarksCard(title = "Resultado", colors = colors) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (result.productCount < 1.0) {
                            Text(
                                text = "No alcanza ni para un helado",
                                style = omnesStyle(16, FontWeight.SemiBold),
                                color = BarksRed
                            )
                        } else {
                            Text(
                                text = "Puedes hacer ${formatCapacity(result.productCount)} helados de ${selectedProduct.name}",
                                style = omnesStyle(16, FontWeight.SemiBold),
                                color = colors.primaryText
                            )
                            if (result.otherIngredientsNeeded.isNotEmpty()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Para esa cantidad necesitarás además:",
                                    style = omnesStyle(12),
                                    color = colors.secondaryText
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    result.otherIngredientsNeeded.forEach { need ->
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
                                                text = "${formatCapacity(need.displayQuantity)} ${need.displayUnit.symbol}",
                                                style = omnesStyle(15, FontWeight.SemiBold),
                                                color = colors.primaryText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerBox(
    text: String,
    placeholder: Boolean,
    enabled: Boolean,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.fieldBackground)
            .border(
                width = 1.dp,
                color = colors.fieldBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = omnesStyle(16, FontWeight.SemiBold),
            color = when {
                !enabled -> colors.secondaryText
                placeholder -> colors.secondaryText
                else -> colors.primaryText
            }
        )
        if (enabled) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatCapacity(q: Double): String {
    val isWhole = q == truncate(q)
    return if (isWhole) q.toLong().toString()
    else {
        val truncated = truncate(q * 10.0) / 10.0
        val s = truncated.toString()
        if (s.contains('.')) {
            val parts = s.split('.')
            val decimals = (parts[1] + "0").take(1)
            "${parts[0]}.$decimals"
        } else "$s.0"
    }
}
