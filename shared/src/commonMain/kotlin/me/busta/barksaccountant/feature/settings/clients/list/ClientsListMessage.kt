package me.busta.barksaccountant.feature.settings.clients.list

import me.busta.barksaccountant.model.Client

sealed interface ClientsListMessage {
    data object Started : ClientsListMessage
    data class ClientsLoaded(val clients: List<Client>) : ClientsListMessage
    data class ErrorOccurred(val error: String) : ClientsListMessage
}
