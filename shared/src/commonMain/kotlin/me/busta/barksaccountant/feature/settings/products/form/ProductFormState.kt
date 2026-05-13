package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.model.ProductIngredient

data class ProductFormState(
    val productId: String? = null,
    val isEditing: Boolean = false,
    val name: String = "",
    val price: String = "",
    val ingredients: List<ProductIngredient> = emptyList(),
    val ingredientQuantityTexts: List<String> = emptyList(),
    val availableIngredients: List<Ingredient> = emptyList(),
    val showIngredientPicker: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean
        get() {
            val basicValid = name.isNotBlank() && price.isNotBlank() &&
                price.toDoubleOrNull() != null && (price.toDoubleOrNull() ?: 0.0) > 0
            if (!basicValid) return false
            val ingredientsValid = ingredientQuantityTexts.all { text ->
                val q = text.toDoubleOrNull()
                q != null && q > 0.0
            }
            return ingredientsValid
        }
}
