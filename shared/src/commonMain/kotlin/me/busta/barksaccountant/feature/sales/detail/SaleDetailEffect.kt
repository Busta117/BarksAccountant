package me.busta.barksaccountant.feature.sales.detail

sealed interface SaleDetailEffect {
    data class LoadSale(val saleId: String) : SaleDetailEffect
    data class SetPaid(val saleId: String) : SaleDetailEffect
    data class SetDelivered(val saleId: String) : SaleDetailEffect
}
