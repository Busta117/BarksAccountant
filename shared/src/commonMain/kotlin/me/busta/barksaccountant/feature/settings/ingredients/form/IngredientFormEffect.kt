package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.model.Ingredient

sealed interface IngredientFormEffect {
    data class LoadIngredient(val ingredientId: String?) : IngredientFormEffect
    data class SaveIngredient(val ingredient: Ingredient) : IngredientFormEffect
    data class UpdateIngredient(val ingredient: Ingredient) : IngredientFormEffect
    data class DeleteIngredient(val id: String) : IngredientFormEffect
}
