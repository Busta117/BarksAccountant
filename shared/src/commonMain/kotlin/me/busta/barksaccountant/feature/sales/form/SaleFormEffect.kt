package me.busta.barksaccountant.feature.sales.form

import me.busta.barksaccountant.model.Sale

sealed interface SaleFormEffect {
    data class LoadFormData(val saleId: String?) : SaleFormEffect
    data class SaveSale(val sale: Sale) : SaleFormEffect
    data class UpdateSale(val sale: Sale) : SaleFormEffect
    data class DeleteSale(val id: String) : SaleFormEffect
}
