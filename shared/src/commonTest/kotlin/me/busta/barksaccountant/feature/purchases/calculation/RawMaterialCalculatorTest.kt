package me.busta.barksaccountant.feature.purchases.calculation

import me.busta.barksaccountant.model.IngredientUnit
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient
import kotlin.test.Test
import kotlin.test.assertEquals

class RawMaterialCalculatorTest {

    private fun product(id: String, name: String, vararg ing: ProductIngredient) =
        Product(id = id, name = name, unitPrice = 0.0, ingredients = ing.toList())

    private fun ing(id: String, name: String, unit: IngredientUnit, qty: Double) =
        ProductIngredient(ingredientId = id, ingredientName = name, unit = unit, quantity = qty)

    @Test
    fun computePlan_sumsSameIngredientAcrossProducts() {
        val pollo = ing("i1", "Pollo", IngredientUnit.GRAMS, 50.0)
        val helado1 = product("p1", "Helado de pollo", pollo)
        val helado2 = product("p2", "Snack pollo", ing("i1", "Pollo", IngredientUnit.GRAMS, 20.0))

        val result = RawMaterialCalculator.computePlan(
            plan = listOf(
                PlanItem("p1", "Helado de pollo", 3),
                PlanItem("p2", "Snack pollo", 2)
            ),
            products = listOf(helado1, helado2)
        )

        assertEquals(1, result.needs.size)
        val need = result.needs.first()
        assertEquals("i1", need.ingredientId)
        assertEquals(190.0, need.totalQuantity)
        assertEquals(IngredientUnit.GRAMS, need.baseUnit)
        assertEquals(IngredientUnit.GRAMS, need.displayUnit)
        assertEquals(190.0, need.displayQuantity)
    }

    @Test
    fun computePlan_convertsGramsToKilograms() {
        val pollo = ing("i1", "Pollo", IngredientUnit.GRAMS, 500.0)
        val helado = product("p1", "Helado de pollo", pollo)

        val result = RawMaterialCalculator.computePlan(
            plan = listOf(PlanItem("p1", "Helado de pollo", 3)),
            products = listOf(helado)
        )

        val need = result.needs.single()
        assertEquals(1500.0, need.totalQuantity)
        assertEquals(IngredientUnit.KILOGRAMS, need.displayUnit)
        assertEquals(1.5, need.displayQuantity)
    }

    @Test
    fun computePlan_convertsMillilitersToLiters() {
        val leche = ing("i2", "Leche", IngredientUnit.MILLILITERS, 250.0)
        val helado = product("p1", "Helado", leche)

        val result = RawMaterialCalculator.computePlan(
            plan = listOf(PlanItem("p1", "Helado", 5)),
            products = listOf(helado)
        )

        val need = result.needs.single()
        assertEquals(1250.0, need.totalQuantity)
        assertEquals(IngredientUnit.LITERS, need.displayUnit)
        assertEquals(1.25, need.displayQuantity)
    }

    @Test
    fun computePlan_unitsNeverConverts() {
        val huevos = ing("i3", "Huevo", IngredientUnit.UNITS, 2.0)
        val helado = product("p1", "Helado", huevos)

        val result = RawMaterialCalculator.computePlan(
            plan = listOf(PlanItem("p1", "Helado", 1000)),
            products = listOf(helado)
        )

        val need = result.needs.single()
        assertEquals(IngredientUnit.UNITS, need.displayUnit)
        assertEquals(2000.0, need.displayQuantity)
    }

    @Test
    fun computePlan_listsProductsWithoutRecipe() {
        val sandia = product("p1", "Helado de sandía")
        val result = RawMaterialCalculator.computePlan(
            plan = listOf(PlanItem("p1", "Helado de sandía", 5)),
            products = listOf(sandia)
        )
        assertEquals(0, result.needs.size)
        assertEquals(listOf("Helado de sandía"), result.productsWithoutRecipe)
    }

    @Test
    fun computeCapacity_returnsDecimalCount() {
        val pollo = ing("i1", "Pollo", IngredientUnit.GRAMS, 27.0)
        val guisantes = ing("i2", "Guisantes", IngredientUnit.GRAMS, 30.0)
        val helado = product("p1", "Helado", pollo, guisantes)

        val result = RawMaterialCalculator.computeCapacity(
            product = helado,
            limitingIngredient = pollo,
            available = 100.0
        )

        assertEquals(100.0 / 27.0, result.productCount, absoluteTolerance = 1e-9)
        assertEquals(1, result.otherIngredientsNeeded.size)
        val guisantesNeed = result.otherIngredientsNeeded.single()
        assertEquals("i2", guisantesNeed.ingredientId)
        assertEquals(30.0 * (100.0 / 27.0), guisantesNeed.totalQuantity, absoluteTolerance = 1e-9)
    }

    @Test
    fun computeCapacity_availableLessThanOneRecipe() {
        val pollo = ing("i1", "Pollo", IngredientUnit.GRAMS, 200.0)
        val helado = product("p1", "Helado", pollo)

        val result = RawMaterialCalculator.computeCapacity(
            product = helado,
            limitingIngredient = pollo,
            available = 50.0
        )

        assertEquals(0.25, result.productCount)
    }

    @Test
    fun computeCapacity_excludesLimitingIngredientFromOthers() {
        val pollo = ing("i1", "Pollo", IngredientUnit.GRAMS, 50.0)
        val leche = ing("i2", "Leche", IngredientUnit.MILLILITERS, 100.0)
        val helado = product("p1", "Helado", pollo, leche)

        val result = RawMaterialCalculator.computeCapacity(
            product = helado,
            limitingIngredient = pollo,
            available = 100.0
        )

        val ids = result.otherIngredientsNeeded.map { it.ingredientId }
        assertEquals(listOf("i2"), ids)
    }
}
