package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksFab
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.android.ui.theme.vagRundschriftStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.clients.list.ClientsListMessage
import me.busta.barksaccountant.feature.settings.clients.list.ClientsListStore
import me.busta.barksaccountant.model.Client

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
    val colors = barksColors()

    LaunchedEffect(Unit) {
        store.dispatch(ClientsListMessage.Started)
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Clientes",
                        style = omnesStyle(20, FontWeight.SemiBold),
                        color = colors.primaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground
                )
            )
        },
        floatingActionButton = {
            BarksFab(onClick = onNewClient)
        },
        containerColor = colors.screenBackground
    ) { padding ->
        when {
            state.isLoading && state.clients.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BarksRed)
                }
            }
            state.error != null && state.clients.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        state.error ?: "",
                        style = omnesStyle(17),
                        color = colors.primaryText.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { store.dispatch(ClientsListMessage.Started) }) {
                        Text(
                            "Reintentar",
                            style = omnesStyle(15, FontWeight.SemiBold),
                            color = BarksRed
                        )
                    }
                }
            }
            state.clients.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay clientes",
                        style = vagRundschriftStyle(20),
                        color = colors.primaryText.copy(alpha = 0.7f)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.screenBackground)
                        .padding(padding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    items(state.clients, key = { it.id }) { client ->
                        ClientCardRow(
                            client = client,
                            onClick = { onClientClick(client.id) },
                            colors = colors
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientCardRow(
    client: Client,
    onClick: () -> Unit,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    val shadowColor = Color.Black.copy(alpha = if (colors.isDark) 0.22f else 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Client name
        Text(
            text = client.name,
            style = omnesStyle(17, FontWeight.SemiBold),
            color = colors.primaryText,
            modifier = Modifier.weight(1f)
        )
    }
}
