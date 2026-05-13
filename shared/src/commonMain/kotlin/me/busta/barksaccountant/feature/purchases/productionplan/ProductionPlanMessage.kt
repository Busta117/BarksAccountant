package me.busta.barksaccountant.feature.purchases.productionplan

import me.busta.barksaccountant.model.Product

sealed interface ProductionPlanMessage {
    data object Started : ProductionPlanMessage
    data class ProductsLoaded(val products: List<Product>) : ProductionPlanMessage
    data object AddProductTapped : ProductionPlanMessage
    data object DismissPicker : ProductionPlanMessage
    data class ProductPicked(val product: Product) : ProductionPlanMessage
    data class QuantityChanged(val productId: String, val text: String) : ProductionPlanMessage
    data class RowRemoved(val productId: String) : ProductionPlanMessage
    data class ErrorOccurred(val error: String) : ProductionPlanMessage
}
