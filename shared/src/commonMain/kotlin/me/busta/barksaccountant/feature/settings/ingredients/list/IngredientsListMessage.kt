package me.busta.barksaccountant.feature.settings.ingredients.list

import me.busta.barksaccountant.model.Ingredient

sealed interface IngredientsListMessage {
    data object Started : IngredientsListMessage
    data class IngredientsLoaded(val ingredients: List<Ingredient>) : IngredientsListMessage
    data class ErrorOccurred(val error: String) : IngredientsListMessage
}
