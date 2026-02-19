package me.busta.barksaccountant.android.ui.screen.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.purchases.form.PurchaseFormMessage
import me.busta.barksaccountant.feature.purchases.form.PurchaseFormStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseFormScreen(
    serviceLocator: ServiceLocator,
    purchaseId: String?,
    personName: String,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember {
        PurchaseFormStore(
            purchaseRepository = serviceLocator.purchaseRepository,
            createdBy = personName
        )
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { store.dispatch(PurchaseFormMessage.Started(purchaseId)) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }
    LaunchedEffect(state.savedSuccessfully) { if (state.savedSuccessfully) onSaved() }
    LaunchedEffect(state.deletedSuccessfully) { if (state.deletedSuccessfully) onSaved() }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        store.dispatch(PurchaseFormMessage.DateChanged(sdf.format(Date(millis))))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(PurchaseFormMessage.DismissDelete) },
            title = { Text("Eliminar Compra") },
            text = { Text("¿Estás seguro de que quieres eliminar esta compra?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(PurchaseFormMessage.ConfirmDelete) }) {
                    Text("Eliminar", color = BarksRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(PurchaseFormMessage.DismissDelete) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Editar Compra" else "Nueva Compra") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.screenBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Info Card (title, description, value)
                BarksCard(title = "Información", colors = colors) {
                    Column {
                        // Title field
                        FieldLabel(text = "Título", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        CustomTextField(
                            value = state.title,
                            onValueChange = { store.dispatch(PurchaseFormMessage.TitleChanged(it)) },
                            placeholder = "Ej: Empaque, ingredientes, hielo...",
                            colors = colors,
                            hasError = state.title.isEmpty(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        // Description field (optional)
                        FieldLabel(text = "Descripción (opcional)", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        CustomTextField(
                            value = state.description,
                            onValueChange = { store.dispatch(PurchaseFormMessage.DescriptionChanged(it)) },
                            placeholder = "Descripción",
                            colors = colors,
                            hasError = false,
                            multiline = true,
                            maxLines = 3
                        )

                        Spacer(Modifier.height(14.dp))

                        // Value field with € prefix
                        FieldLabel(text = "Valor", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(colors.fieldBackground, RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (state.value.isEmpty()) BarksRed.copy(alpha = 0.45f) else colors.fieldBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "€",
                                style = omnesStyle(17, FontWeight.SemiBold),
                                color = colors.secondaryText
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = state.value,
                                onValueChange = { store.dispatch(PurchaseFormMessage.ValueChanged(it)) },
                                textStyle = omnesStyle(17, FontWeight.SemiBold).copy(color = colors.primaryText),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (state.value.isEmpty()) {
                                        Text(
                                            text = "0.00",
                                            style = omnesStyle(17, FontWeight.SemiBold),
                                            color = colors.secondaryText.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Date Card
                BarksCard(title = "Fecha", colors = colors) {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(
                            text = state.date,
                            style = omnesStyle(16, FontWeight.Normal),
                            color = colors.primaryText
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Save Card
                BarksCard(colors = colors) {
                    Column {
                        Button(
                            onClick = { store.dispatch(PurchaseFormMessage.SaveTapped) },
                            enabled = state.canSave && !state.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = BarksRed,
                                contentColor = BarksWhite,
                                disabledContainerColor = BarksRed.copy(alpha = 0.6f),
                                disabledContentColor = BarksWhite.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (state.isSaving) {
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

                        state.error?.let { error ->
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = error,
                                style = omnesStyle(13, FontWeight.Normal),
                                color = BarksRed,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Delete Card (only in edit mode)
                if (state.isEditing) {
                    Spacer(Modifier.height(12.dp))
                    BarksCard(colors = colors) {
                        OutlinedButton(
                            onClick = { store.dispatch(PurchaseFormMessage.DeleteTapped) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, BarksRed),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = BarksRed
                            )
                        ) {
                            Text(
                                text = "Eliminar Compra",
                                style = omnesStyle(16, FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    Text(
        text = text,
        style = omnesStyle(13, FontWeight.Normal),
        color = colors.secondaryText
    )
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    hasError: Boolean,
    modifier: Modifier = Modifier,
    multiline: Boolean = false,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = omnesStyle(
            size = if (multiline) 16 else 17,
            weight = if (multiline) FontWeight.Normal else FontWeight.SemiBold
        ).copy(color = colors.primaryText),
        keyboardOptions = keyboardOptions,
        singleLine = !multiline,
        maxLines = if (multiline) maxLines else 1,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (multiline) {
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                } else {
                    Modifier
                        .height(48.dp)
                        .padding(horizontal = 12.dp)
                }
            )
            .background(colors.fieldBackground, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (hasError) BarksRed.copy(alpha = 0.45f) else colors.fieldBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (!multiline) {
                    Modifier.padding(horizontal = 12.dp)
                } else {
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                }
            ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = omnesStyle(
                            size = if (multiline) 16 else 17,
                            weight = if (multiline) FontWeight.Normal else FontWeight.SemiBold
                        ),
                        color = colors.secondaryText.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        }
    )
}
