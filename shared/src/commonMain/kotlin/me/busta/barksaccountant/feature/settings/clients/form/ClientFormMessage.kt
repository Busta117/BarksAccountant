package me.busta.barksaccountant.feature.settings.clients.form

import me.busta.barksaccountant.model.Client

sealed interface ClientFormMessage {
    data class Started(val clientId: String?) : ClientFormMessage
    data class ClientLoaded(val client: Client?) : ClientFormMessage
    data class NameChanged(val text: String) : ClientFormMessage
    data class ResponsibleChanged(val text: String) : ClientFormMessage
    data class NifChanged(val text: String) : ClientFormMessage
    data class AddressChanged(val text: String) : ClientFormMessage
    data object SaveTapped : ClientFormMessage
    data object SaveSuccess : ClientFormMessage
    data object DeleteTapped : ClientFormMessage
    data object ConfirmDelete : ClientFormMessage
    data object DismissDelete : ClientFormMessage
    data object DeleteSuccess : ClientFormMessage
    data class ErrorOccurred(val error: String) : ClientFormMessage
}
