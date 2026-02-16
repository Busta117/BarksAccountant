package me.busta.barksaccountant.feature.settings.products.form

data class ProductFormState(
    val productId: String? = null,
    val isEditing: Boolean = false,
    val name: String = "",
    val price: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean get() = name.isNotBlank() && price.isNotBlank() &&
        price.toDoubleOrNull() != null && (price.toDoubleOrNull() ?: 0.0) > 0
}
