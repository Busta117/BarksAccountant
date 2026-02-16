package me.busta.barksaccountant.feature.settings.products.list

import me.busta.barksaccountant.model.Product

data class ProductsListState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
