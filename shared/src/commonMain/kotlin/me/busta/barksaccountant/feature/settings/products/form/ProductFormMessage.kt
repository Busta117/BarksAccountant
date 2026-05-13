package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.model.Product

sealed interface ProductFormMessage {
    data class Started(val productId: String?) : ProductFormMessage
    data class ProductLoaded(val product: Product?) : ProductFormMessage
    data class IngredientsLoaded(val ingredients: List<Ingredient>) : ProductFormMessage
    data class NameChanged(val text: String) : ProductFormMessage
    data class PriceChanged(val text: String) : ProductFormMessage
    data object AddIngredientTapped : ProductFormMessage
    data object DismissIngredientPicker : ProductFormMessage
    data class IngredientPicked(val ingredient: Ingredient) : ProductFormMessage
    data class IngredientQuantityChanged(val index: Int, val text: String) : ProductFormMessage
    data class IngredientRemoved(val index: Int) : ProductFormMessage
    data object SaveTapped : ProductFormMessage
    data object SaveSuccess : ProductFormMessage
    data object DeleteTapped : ProductFormMessage
    data object ConfirmDelete : ProductFormMessage
    data object DismissDelete : ProductFormMessage
    data object DeleteSuccess : ProductFormMessage
    data class ErrorOccurred(val error: String) : ProductFormMessage
}
