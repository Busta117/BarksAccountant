package me.busta.barksaccountant.android.ui.screen.settings

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.products.form.ProductFormMessage
import me.busta.barksaccountant.feature.settings.products.form.ProductFormStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    serviceLocator: ServiceLocator,
    productId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember { ProductFormStore(productRepository = serviceLocator.productRepository) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) { store.dispatch(ProductFormMessage.Started(productId)) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }
    LaunchedEffect(state.savedSuccessfully) { if (state.savedSuccessfully) onSaved() }
    LaunchedEffect(state.deletedSuccessfully) { if (state.deletedSuccessfully) onSaved() }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(ProductFormMessage.DismissDelete) },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que quieres eliminar este producto?") },
            confirmButton = { TextButton(onClick = { store.dispatch(ProductFormMessage.ConfirmDelete) }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { store.dispatch(ProductFormMessage.DismissDelete) }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Editar Producto" else "Nuevo Producto") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text("Nombre", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.name, onValueChange = { store.dispatch(ProductFormMessage.NameChanged(it)) }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Precio", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.price, onValueChange = { store.dispatch(ProductFormMessage.PriceChanged(it)) }, label = { Text("Precio") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))
            Button(onClick = { store.dispatch(ProductFormMessage.SaveTapped) }, enabled = state.canSave && !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Guardar", fontWeight = FontWeight.SemiBold)
            }

            if (state.isEditing) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { store.dispatch(ProductFormMessage.DeleteTapped) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Eliminar Producto")
                }
            }

            state.error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
