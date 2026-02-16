package me.busta.barksaccountant.feature.settings.products.list

sealed interface ProductsListEffect {
    data object LoadProducts : ProductsListEffect
}
