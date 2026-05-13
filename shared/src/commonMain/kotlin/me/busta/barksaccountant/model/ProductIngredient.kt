package me.busta.barksaccountant.model

data class ProductIngredient(
    val ingredientId: String,
    val ingredientName: String,
    val unit: IngredientUnit,
    val quantity: Double
)
