package me.busta.barksaccountant.feature.settings.clients.form

data class ClientFormState(
    val clientId: String? = null,
    val isEditing: Boolean = false,
    val name: String = "",
    val responsible: String = "",
    val nif: String = "",
    val address: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean get() = name.isNotBlank()
}
