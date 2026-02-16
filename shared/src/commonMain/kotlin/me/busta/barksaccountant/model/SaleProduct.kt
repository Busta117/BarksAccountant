package me.busta.barksaccountant.model

data class SaleProduct(
    val productId: String,
    val name: String,
    val unitPrice: Double,
    val quantity: Int
) {
    val totalPrice: Double get() = unitPrice * quantity
}
