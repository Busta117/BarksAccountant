package me.busta.barksaccountant.android.ui.screen.purchases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Titulo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = { store.dispatch(PurchaseFormMessage.TitleChanged(it)) },
                label = { Text("Titulo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Descripción (opcional)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = { store.dispatch(PurchaseFormMessage.DescriptionChanged(it)) },
                label = { Text("Descripción") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Valor", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.value,
                onValueChange = { store.dispatch(PurchaseFormMessage.ValueChanged(it)) },
                label = { Text("Valor") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Fecha", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Fecha: ${state.date}")
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { store.dispatch(PurchaseFormMessage.SaveTapped) },
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

            if (state.isEditing) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { store.dispatch(PurchaseFormMessage.DeleteTapped) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar Compra")
                }
            }

            state.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
