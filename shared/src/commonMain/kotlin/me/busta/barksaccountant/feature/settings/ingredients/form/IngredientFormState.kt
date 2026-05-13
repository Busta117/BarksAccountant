package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.model.IngredientUnit

data class IngredientFormState(
    val ingredientId: String? = null,
    val isEditing: Boolean = false,
    val name: String = "",
    val unit: IngredientUnit = IngredientUnit.GRAMS,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deleteBlockedBy: List<String> = emptyList(),
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}
