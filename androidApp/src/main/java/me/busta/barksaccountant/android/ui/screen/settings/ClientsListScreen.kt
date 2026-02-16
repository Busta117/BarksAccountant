package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.clients.list.ClientsListMessage
import me.busta.barksaccountant.feature.settings.clients.list.ClientsListStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsListScreen(
    serviceLocator: ServiceLocator,
    onClientClick: (String) -> Unit,
    onNewClient: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember { ClientsListStore(clientRepository = serviceLocator.clientRepository) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) { store.dispatch(ClientsListMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } },
                actions = { IconButton(onClick = onNewClient) { Icon(Icons.Default.Add, contentDescription = "Nuevo cliente") } }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.clients.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            state.error != null && state.clients.isEmpty() -> {
                Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { store.dispatch(ClientsListMessage.Started) }) { Text("Reintentar") }
                }
            }
            state.clients.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No hay clientes", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(state.clients, key = { it.id }) { client ->
                        Column(
                            modifier = Modifier.fillMaxWidth().clickable { onClientClick(client.id) }.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(client.name, style = MaterialTheme.typography.bodyLarge)
                            client.responsible?.let { if (it.isNotEmpty()) Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
