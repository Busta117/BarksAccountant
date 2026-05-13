package me.busta.barksaccountant.feature.purchases.calculation

import me.busta.barksaccountant.model.IngredientUnit

data class PlanItem(
    val productId: String,
    val productName: String,
    val quantity: Int
)

data class RawMaterialNeed(
    val ingredientId: String,
    val ingredientName: String,
    val baseUnit: IngredientUnit,
    val totalQuantity: Double,
    val displayUnit: IngredientUnit,
    val displayQuantity: Double
)

data class PlanResult(
    val needs: List<RawMaterialNeed>,
    val productsWithoutRecipe: List<String>
)

data class CapacityResult(
    val productCount: Double,
    val otherIngredientsNeeded: List<RawMaterialNeed>
)
