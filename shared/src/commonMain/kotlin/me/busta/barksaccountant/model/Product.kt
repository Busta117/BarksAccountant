package me.busta.barksaccountant.model

data class Product(
    val id: String,
    val name: String,
    val unitPrice: Double,
    val ingredients: List<ProductIngredient> = emptyList()
)
