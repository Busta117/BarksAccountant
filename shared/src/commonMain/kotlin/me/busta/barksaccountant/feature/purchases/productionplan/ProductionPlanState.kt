package me.busta.barksaccountant.feature.purchases.productionplan

import me.busta.barksaccountant.feature.purchases.calculation.PlanItem
import me.busta.barksaccountant.feature.purchases.calculation.PlanResult
import me.busta.barksaccountant.feature.purchases.calculation.RawMaterialCalculator
import me.busta.barksaccountant.model.Product

data class PlanRow(
    val productId: String,
    val productName: String,
    val quantityText: String
)

data class ProductionPlanState(
    val availableProducts: List<Product> = emptyList(),
    val rows: List<PlanRow> = emptyList(),
    val showProductPicker: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val result: PlanResult
        get() = RawMaterialCalculator.computePlan(
            plan = rows.mapNotNull { row ->
                val qty = row.quantityText.toIntOrNull() ?: 0
                if (qty <= 0) null else PlanItem(row.productId, row.productName, qty)
            },
            products = availableProducts
        )
}
