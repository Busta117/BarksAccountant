package me.busta.barksaccountant.feature.sales.form

import me.busta.barksaccountant.model.Client
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.SaleProduct

data class SaleFormState(
    val saleId: String? = null,
    val isEditing: Boolean = false,
    val clientName: String = "",
    val orderDate: String = "",
    val deliveryDate: String? = null,
    val products: List<SaleProduct> = emptyList(),
    val clients: List<Client> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val totalPrice: Double get() = products.sumOf { it.totalPrice }
    val canSave: Boolean get() = clientName.isNotBlank() && products.isNotEmpty()
}
