package me.busta.barksaccountant.feature.settings.clients.form

import me.busta.barksaccountant.model.Client

sealed interface ClientFormEffect {
    data class LoadClient(val clientId: String?) : ClientFormEffect
    data class SaveClient(val client: Client) : ClientFormEffect
    data class UpdateClient(val client: Client) : ClientFormEffect
    data class DeleteClient(val id: String) : ClientFormEffect
}
