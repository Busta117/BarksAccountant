package me.busta.barksaccountant.feature.purchases.productioncapacity

import me.busta.barksaccountant.feature.purchases.calculation.CapacityResult
import me.busta.barksaccountant.feature.purchases.calculation.RawMaterialCalculator
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient

data class ProductionCapacityState(
    val availableProducts: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val selectedIngredient: ProductIngredient? = null,
    val availableQuantityText: String = "",
    val showProductPicker: Boolean = false,
    val showIngredientPicker: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val result: CapacityResult?
        get() {
            val product = selectedProduct ?: return null
            val ingredient = selectedIngredient ?: return null
            val available = availableQuantityText.toDoubleOrNull() ?: return null
            if (available <= 0.0) return null
            return RawMaterialCalculator.computeCapacity(product, ingredient, available)
        }
}
