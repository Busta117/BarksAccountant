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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.ingredients.form.IngredientFormMessage
import me.busta.barksaccountant.feature.settings.ingredients.form.IngredientFormStore
import me.busta.barksaccountant.model.IngredientUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientFormScreen(
    serviceLocator: ServiceLocator,
    ingredientId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember {
        IngredientFormStore(
            ingredientRepository = serviceLocator.ingredientRepository,
            productRepository = serviceLocator.productRepository
        )
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) {
        store.dispatch(IngredientFormMessage.Started(ingredientId))
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onSaved()
    }

    LaunchedEffect(state.deletedSuccessfully) {
        if (state.deletedSuccessfully) onSaved()
    }

    // Delete confirmation
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(IngredientFormMessage.DismissDelete) },
            title = { Text("Eliminar ingrediente") },
            text = { Text("¿Estás seguro de que quieres eliminar este ingrediente?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(IngredientFormMessage.ConfirmDelete) }) {
                    Text("Eliminar", color = BarksRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(IngredientFormMessage.DismissDelete) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete blocked dialog
    if (state.deleteBlockedBy.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { store.dispatch(IngredientFormMessage.DismissDeleteBlocked) },
            title = { Text("No se puede eliminar") },
            text = {
                Text(
                    "Este ingrediente se usa en: ${state.deleteBlockedBy.joinToString(", ")}. " +
                        "Elimínalo de esos productos primero."
                )
            },
            confirmButton = {
                TextButton(onClick = { store.dispatch(IngredientFormMessage.DismissDeleteBlocked) }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Editar Ingrediente" else "Nuevo Ingrediente",
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
            InfoCard(
                name = state.name,
                unit = state.unit,
                isEditing = state.isEditing,
                colors = colors,
                onNameChange = { store.dispatch(IngredientFormMessage.NameChanged(it)) },
                onUnitChange = { store.dispatch(IngredientFormMessage.UnitChanged(it)) }
            )

            SaveCard(
                canSave = state.canSave,
                isSaving = state.isSaving,
                error = state.error,
                colors = colors,
                onSave = { store.dispatch(IngredientFormMessage.SaveTapped) }
            )

            if (state.isEditing) {
                DeleteCard(
                    colors = colors,
                    onDelete = { store.dispatch(IngredientFormMessage.DeleteTapped) }
                )
            }
        }
    }
}

// ─── Info Card ───────────────────────────────────────────────────────────────

private fun unitLabel(unit: IngredientUnit): String = when (unit) {
    IngredientUnit.GRAMS -> "gramos"
    IngredientUnit.KILOGRAMS -> "kilos"
    IngredientUnit.MILLILITERS -> "mililitros"
    IngredientUnit.LITERS -> "litros"
    IngredientUnit.UNITS -> "unidades"
}

@Composable
private fun InfoCard(
    name: String,
    unit: IngredientUnit,
    isEditing: Boolean,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onNameChange: (String) -> Unit,
    onUnitChange: (IngredientUnit) -> Unit
) {
    BarksCard(title = "Información", colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Name field
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
                                    text = "Ej: Azúcar",
                                    style = omnesStyle(17, FontWeight.SemiBold),
                                    color = colors.secondaryText.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Unit picker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Unidad",
                    style = omnesStyle(13),
                    color = colors.secondaryText
                )

                var expanded by remember { mutableStateOf(false) }

                Box {
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
                            .clickable(enabled = !isEditing) { expanded = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${unitLabel(unit)} (${unit.symbol})",
                            style = omnesStyle(17, FontWeight.SemiBold),
                            color = if (isEditing) colors.primaryText.copy(alpha = 0.6f) else colors.primaryText
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = colors.secondaryText
                        )
                    }

                    DropdownMenu(
                        expanded = expanded && !isEditing,
                        onDismissRequest = { expanded = false }
                    ) {
                        IngredientUnit.entries.forEach { u ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${unitLabel(u)} (${u.symbol})",
                                        style = omnesStyle(16)
                                    )
                                },
                                onClick = {
                                    onUnitChange(u)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (isEditing) {
                    Text(
                        text = "La unidad no se puede cambiar",
                        style = omnesStyle(12),
                        color = colors.secondaryText
                    )
                }
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
                text = "Eliminar ingrediente",
                style = omnesStyle(16, FontWeight.SemiBold),
                color = BarksRed
            )
        }
    }
}
