package me.busta.barksaccountant.feature.purchases.calculation

import me.busta.barksaccountant.model.IngredientUnit
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient

object RawMaterialCalculator {

    fun computePlan(plan: List<PlanItem>, products: List<Product>): PlanResult {
        val productsById = products.associateBy { it.id }
        val aggregated = mutableMapOf<String, AggregatedIngredient>()
        val withoutRecipe = mutableListOf<String>()

        for (item in plan) {
            if (item.quantity <= 0) continue
            val product = productsById[item.productId] ?: continue
            if (product.ingredients.isEmpty()) {
                withoutRecipe += product.name
                continue
            }
            for (recipeIng in product.ingredients) {
                val current = aggregated[recipeIng.ingredientId]
                val addedQty = recipeIng.quantity * item.quantity
                aggregated[recipeIng.ingredientId] = AggregatedIngredient(
                    name = recipeIng.ingredientName,
                    unit = recipeIng.unit,
                    totalQuantity = (current?.totalQuantity ?: 0.0) + addedQty
                )
            }
        }

        val needs = aggregated.map { (id, agg) ->
            val (displayUnit, displayQty) = toDisplay(agg.unit, agg.totalQuantity)
            RawMaterialNeed(
                ingredientId = id,
                ingredientName = agg.name,
                baseUnit = agg.unit,
                totalQuantity = agg.totalQuantity,
                displayUnit = displayUnit,
                displayQuantity = displayQty
            )
        }.sortedBy { it.ingredientName.lowercase() }

        return PlanResult(needs = needs, productsWithoutRecipe = withoutRecipe)
    }

    fun computeCapacity(
        product: Product,
        limitingIngredient: ProductIngredient,
        available: Double
    ): CapacityResult {
        if (limitingIngredient.quantity <= 0.0 || available < 0.0) {
            return CapacityResult(productCount = 0.0, otherIngredientsNeeded = emptyList())
        }
        val count = available / limitingIngredient.quantity
        val others = product.ingredients
            .filter { it.ingredientId != limitingIngredient.ingredientId }
            .map { recipeIng ->
                val total = recipeIng.quantity * count
                val (displayUnit, displayQty) = toDisplay(recipeIng.unit, total)
                RawMaterialNeed(
                    ingredientId = recipeIng.ingredientId,
                    ingredientName = recipeIng.ingredientName,
                    baseUnit = recipeIng.unit,
                    totalQuantity = total,
                    displayUnit = displayUnit,
                    displayQuantity = displayQty
                )
            }
            .sortedBy { it.ingredientName.lowercase() }
        return CapacityResult(productCount = count, otherIngredientsNeeded = others)
    }

    private fun toDisplay(unit: IngredientUnit, qty: Double): Pair<IngredientUnit, Double> {
        return when (unit) {
            IngredientUnit.GRAMS -> if (qty >= 1000.0) IngredientUnit.KILOGRAMS to (qty / 1000.0) else unit to qty
            IngredientUnit.MILLILITERS -> if (qty >= 1000.0) IngredientUnit.LITERS to (qty / 1000.0) else unit to qty
            else -> unit to qty
        }
    }

    private data class AggregatedIngredient(
        val name: String,
        val unit: IngredientUnit,
        val totalQuantity: Double
    )
}
