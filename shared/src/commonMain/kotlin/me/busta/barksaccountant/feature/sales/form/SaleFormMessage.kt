package me.busta.barksaccountant.feature.sales.form

import me.busta.barksaccountant.model.Client
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.Sale
import me.busta.barksaccountant.model.SaleProduct

sealed interface SaleFormMessage {
    data class Started(val saleId: String?) : SaleFormMessage
    data class DataLoaded(
        val clients: List<Client>,
        val products: List<Product>,
        val sale: Sale?
    ) : SaleFormMessage
    data class ClientSelected(val name: String) : SaleFormMessage
    data class ResponsibleChanged(val text: String) : SaleFormMessage
    data class OrderDateChanged(val date: String) : SaleFormMessage
    data class DeliveryDateChanged(val date: String?) : SaleFormMessage
    data class AddProduct(val product: Product) : SaleFormMessage
    data class RemoveProduct(val index: Int) : SaleFormMessage
    data class IncrementQuantity(val index: Int) : SaleFormMessage
    data class DecrementQuantity(val index: Int) : SaleFormMessage
    data object SaveTapped : SaleFormMessage
    data object SaveSuccess : SaleFormMessage
    data class ErrorOccurred(val error: String) : SaleFormMessage
}
