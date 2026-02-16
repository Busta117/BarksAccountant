package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.clients.form.ClientFormMessage
import me.busta.barksaccountant.feature.settings.clients.form.ClientFormStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen(
    serviceLocator: ServiceLocator,
    clientId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember { ClientFormStore(clientRepository = serviceLocator.clientRepository) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) { store.dispatch(ClientFormMessage.Started(clientId)) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }
    LaunchedEffect(state.savedSuccessfully) { if (state.savedSuccessfully) onSaved() }
    LaunchedEffect(state.deletedSuccessfully) { if (state.deletedSuccessfully) onSaved() }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(ClientFormMessage.DismissDelete) },
            title = { Text("Eliminar Cliente") },
            text = { Text("¿Estás seguro de que quieres eliminar este cliente?") },
            confirmButton = { TextButton(onClick = { store.dispatch(ClientFormMessage.ConfirmDelete) }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { store.dispatch(ClientFormMessage.DismissDelete) }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Editar Cliente" else "Nuevo Cliente") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text("Nombre", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.name, onValueChange = { store.dispatch(ClientFormMessage.NameChanged(it)) }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Responsable (opcional)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.responsible, onValueChange = { store.dispatch(ClientFormMessage.ResponsibleChanged(it)) }, label = { Text("Responsable") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("NIF (opcional)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.nif, onValueChange = { store.dispatch(ClientFormMessage.NifChanged(it)) }, label = { Text("NIF") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Dirección (opcional)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.address, onValueChange = { store.dispatch(ClientFormMessage.AddressChanged(it)) }, label = { Text("Dirección") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))
            Button(onClick = { store.dispatch(ClientFormMessage.SaveTapped) }, enabled = state.canSave && !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Guardar", fontWeight = FontWeight.SemiBold)
            }

            if (state.isEditing) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { store.dispatch(ClientFormMessage.DeleteTapped) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Eliminar Cliente")
                }
            }

            state.error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
