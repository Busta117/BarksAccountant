package me.busta.barksaccountant.feature.settings.clients.list

sealed interface ClientsListEffect {
    data object LoadClients : ClientsListEffect
}
