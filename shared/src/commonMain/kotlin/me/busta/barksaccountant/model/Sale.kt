package me.busta.barksaccountant.model

data class Sale(
    val id: String,
    val clientName: String,
    val responsible: String?,
    val orderDate: String,
    val deliveryDate: String?,
    val products: List<SaleProduct>,
    val totalPrice: Double,
    val isPaid: Boolean,
    val isDelivered: Boolean
)
