package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.model.IngredientUnit

sealed interface IngredientFormMessage {
    data class Started(val ingredientId: String?) : IngredientFormMessage
    data class IngredientLoaded(val ingredient: Ingredient?) : IngredientFormMessage
    data class NameChanged(val text: String) : IngredientFormMessage
    data class UnitChanged(val unit: IngredientUnit) : IngredientFormMessage
    data object SaveTapped : IngredientFormMessage
    data object SaveSuccess : IngredientFormMessage
    data object DeleteTapped : IngredientFormMessage
    data object ConfirmDelete : IngredientFormMessage
    data object DismissDelete : IngredientFormMessage
    data object DismissDeleteBlocked : IngredientFormMessage
    data class DeleteBlockedBy(val productNames: List<String>) : IngredientFormMessage
    data object DeleteSuccess : IngredientFormMessage
    data class ErrorOccurred(val error: String) : IngredientFormMessage
}
