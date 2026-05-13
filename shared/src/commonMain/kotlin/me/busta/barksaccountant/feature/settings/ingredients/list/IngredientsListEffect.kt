package me.busta.barksaccountant.feature.settings.ingredients.list

sealed interface IngredientsListEffect {
    data object LoadIngredients : IngredientsListEffect
}
