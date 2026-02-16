package me.busta.barksaccountant.feature.settings.products.list

import me.busta.barksaccountant.model.Product

sealed interface ProductsListMessage {
    data object Started : ProductsListMessage
    data class ProductsLoaded(val products: List<Product>) : ProductsListMessage
    data class ErrorOccurred(val error: String) : ProductsListMessage
}
