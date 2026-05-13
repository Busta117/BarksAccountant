package me.busta.barksaccountant.android.ui.screen.settings

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.products.form.ProductFormMessage
import me.busta.barksaccountant.feature.settings.products.form.ProductFormStore
import me.busta.barksaccountant.model.ProductIngredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    serviceLocator: ServiceLocator,
    productId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember {
        ProductFormStore(
            productRepository = serviceLocator.productRepository,
            ingredientRepository = serviceLocator.ingredientRepository
        )
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) {
        store.dispatch(ProductFormMessage.Started(productId))
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

    // Delete confirmation dialog
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(ProductFormMessage.DismissDelete) },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que quieres eliminar este producto?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(ProductFormMessage.ConfirmDelete) }) {
                    Text("Eliminar", color = BarksRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(ProductFormMessage.DismissDelete) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (state.showIngredientPicker) {
        IngredientPickerDialog(
            ingredients = state.availableIngredients.filter { a -> state.ingredients.none { it.ingredientId == a.id } },
            onSelected = { ing -> store.dispatch(ProductFormMessage.IngredientPicked(ing)) },
            onDismiss = { store.dispatch(ProductFormMessage.DismissIngredientPicker) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Editar Producto" else "Nuevo Producto",
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
            // Info Card
            InfoCard(
                name = state.name,
                price = state.price,
                colors = colors,
                onNameChange = { store.dispatch(ProductFormMessage.NameChanged(it)) },
                onPriceChange = { store.dispatch(ProductFormMessage.PriceChanged(it)) }
            )

            // Ingredients Card
            IngredientsCard(
                ingredients = state.ingredients,
                quantityTexts = state.ingredientQuantityTexts,
                colors = colors,
                catalogEmpty = state.availableIngredients.isEmpty(),
                onAdd = { store.dispatch(ProductFormMessage.AddIngredientTapped) },
                onQuantityChange = { index, text ->
                    store.dispatch(ProductFormMessage.IngredientQuantityChanged(index, text))
                },
                onRemove = { index -> store.dispatch(ProductFormMessage.IngredientRemoved(index)) }
            )

            // Save Card
            SaveCard(
                canSave = state.canSave,
                isSaving = state.isSaving,
                error = state.error,
                colors = colors,
                onSave = { store.dispatch(ProductFormMessage.SaveTapped) }
            )

            // Delete Card (only in edit mode)
            if (state.isEditing) {
                DeleteCard(
                    colors = colors,
                    onDelete = { store.dispatch(ProductFormMessage.DeleteTapped) }
                )
            }
        }
    }
}

// ─── Info Card ───────────────────────────────────────────────────────────────

@Composable
private fun InfoCard(
    name: String,
    price: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit
) {
    BarksCard(title = "Información", colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Name Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nombre",
                    style = omnesStyle(13),
                    color = colors.secondaryText
                )

                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    textStyle = omnesStyle(17, FontWeight.SemiBold).copy(
                        color = colors.primaryText
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.fieldBackground)
                        .border(
                            width = 1.dp,
                            color = if (name.isEmpty()) BarksRed.copy(alpha = 0.45f) else colors.fieldBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (name.isEmpty()) {
                                Text(
                                    text = "Ej: Paleta Sandía",
                                    style = omnesStyle(17, FontWeight.SemiBold),
                                    color = colors.secondaryText.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Price Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Precio",
                    style = omnesStyle(13),
                    color = colors.secondaryText
                )

                BasicTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    textStyle = omnesStyle(17, FontWeight.SemiBold).copy(
                        color = colors.primaryText
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.fieldBackground)
                        .border(
                            width = 1.dp,
                            color = if (price.isEmpty()) BarksRed.copy(alpha = 0.45f) else colors.fieldBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "€",
                                style = omnesStyle(17, FontWeight.SemiBold),
                                color = colors.secondaryText
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (price.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        style = omnesStyle(17, FontWeight.SemiBold),
                                        color = colors.secondaryText.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
        }
    }
}

// ─── Save Card ───────────────────────────────────────────────────────────────

@Composable
private fun SaveCard(
    canSave: Boolean,
    isSaving: Boolean,
    error: String?,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onSave: () -> Unit
) {
    BarksCard(colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (canSave && !isSaving) BarksRed else BarksRed.copy(alpha = 0.6f)
                    )
                    .clickable(enabled = canSave && !isSaving, onClick = onSave),
                contentAlignment = Alignment.Center
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
                        style = omnesStyle(16, FontWeight.SemiBold),
                        color = BarksWhite
                    )
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    style = omnesStyle(13),
                    color = BarksRed,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

// ─── Ingredients Card ────────────────────────────────────────────────────────

@Composable
private fun IngredientsCard(
    ingredients: List<ProductIngredient>,
    quantityTexts: List<String>,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    catalogEmpty: Boolean,
    onAdd: () -> Unit,
    onQuantityChange: (Int, String) -> Unit,
    onRemove: (Int) -> Unit
) {
    BarksCard(title = "Ingredientes", colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (ingredients.isEmpty()) {
                Text(
                    text = "No hay ingredientes añadidos",
                    style = omnesStyle(14),
                    color = colors.secondaryText
                )
            } else {
                ingredients.forEachIndexed { index, ing ->
                    val qtyText = quantityTexts.getOrNull(index) ?: ""
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = ing.ingredientName,
                            style = omnesStyle(15, FontWeight.SemiBold),
                            color = colors.primaryText,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )

                        BasicTextField(
                            value = qtyText,
                            onValueChange = { onQuantityChange(index, it) },
                            textStyle = omnesStyle(15, FontWeight.SemiBold).copy(
                                color = colors.primaryText
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .width(80.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.fieldBackground)
                                .border(
                                    width = 1.dp,
                                    color = if (qtyText.toDoubleOrNull()?.let { it > 0.0 } != true)
                                        BarksRed.copy(alpha = 0.45f)
                                    else colors.fieldBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (qtyText.isEmpty()) {
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

                        Box(
                            modifier = Modifier.width(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ing.unit.symbol,
                                style = omnesStyle(14, FontWeight.SemiBold),
                                color = colors.secondaryText
                            )
                        }

                        IconButton(
                            onClick = { onRemove(index) },
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
                        color = if (catalogEmpty) colors.secondaryText.copy(alpha = 0.4f) else BarksRed,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = !catalogEmpty, onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (catalogEmpty) "No hay ingredientes en el catálogo" else "+ Añadir ingrediente",
                    style = omnesStyle(14, FontWeight.SemiBold),
                    color = if (catalogEmpty) colors.secondaryText else BarksRed
                )
            }

            if (catalogEmpty) {
                Text(
                    text = "Créalos en Settings → Ingredientes",
                    style = omnesStyle(12),
                    color = colors.secondaryText
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
                text = "Eliminar producto",
                style = omnesStyle(16, FontWeight.SemiBold),
                color = BarksRed
            )
        }
    }
}
