package me.busta.barksaccountant.feature.purchases.form

data class PurchaseFormState(
    val purchaseId: String? = null,
    val isEditing: Boolean = false,
    val title: String = "",
    val description: String = "",
    val value: String = "",
    val date: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean get() = title.isNotBlank() && value.isNotBlank() &&
        value.toDoubleOrNull() != null && (value.toDoubleOrNull() ?: 0.0) > 0
}
