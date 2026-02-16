package me.busta.barksaccountant.feature.settings.clients.list

import me.busta.barksaccountant.model.Client

data class ClientsListState(
    val clients: List<Client> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
