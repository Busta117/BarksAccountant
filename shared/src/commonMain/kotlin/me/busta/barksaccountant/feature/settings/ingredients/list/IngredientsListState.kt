package me.busta.barksaccountant.feature.settings.ingredients.list

import me.busta.barksaccountant.model.Ingredient

data class IngredientsListState(
    val ingredients: List<Ingredient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
