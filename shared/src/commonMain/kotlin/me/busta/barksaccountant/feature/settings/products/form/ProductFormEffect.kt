package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.model.Product

sealed interface ProductFormEffect {
    data class LoadProduct(val productId: String?) : ProductFormEffect
    data class SaveProduct(val product: Product) : ProductFormEffect
    data class UpdateProduct(val product: Product) : ProductFormEffect
    data class DeleteProduct(val id: String) : ProductFormEffect
}
