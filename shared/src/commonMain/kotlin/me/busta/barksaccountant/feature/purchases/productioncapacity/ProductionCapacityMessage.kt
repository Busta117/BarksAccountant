package me.busta.barksaccountant.feature.purchases.productioncapacity

import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient

sealed interface ProductionCapacityMessage {
    data object Started : ProductionCapacityMessage
    data class ProductsLoaded(val products: List<Product>) : ProductionCapacityMessage
    data object ProductPickerOpened : ProductionCapacityMessage
    data object DismissProductPicker : ProductionCapacityMessage
    data class ProductPicked(val product: Product) : ProductionCapacityMessage
    data object IngredientPickerOpened : ProductionCapacityMessage
    data object DismissIngredientPicker : ProductionCapacityMessage
    data class IngredientPicked(val ingredient: ProductIngredient) : ProductionCapacityMessage
    data class AvailableQuantityChanged(val text: String) : ProductionCapacityMessage
    data class ErrorOccurred(val error: String) : ProductionCapacityMessage
}
