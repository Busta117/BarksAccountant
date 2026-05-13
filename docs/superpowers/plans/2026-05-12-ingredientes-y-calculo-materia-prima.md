# Ingredientes y cálculo de materia prima — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Añadir un catálogo global de ingredientes, permitir asignar una receta (lista de ingredientes con cantidad) a cada producto, y añadir dos herramientas de cálculo de materia prima dentro de Compras (directo e inverso).

**Architecture:** Sigue el patrón UDF existente: `Store<State, Message, Effect>` en `shared/commonMain`, `@Observable StoreWrapper` en iOS, `collectAsState()` en Compose. Lógica de cálculo pura en `shared/commonMain/feature/purchases/calculation/` con tests unitarios en `commonTest`. Catálogo de ingredientes como colección Firestore separada (`apps/{appId}/ingredients`). Receta embebida en `Product.ingredients` denormalizada.

**Tech Stack:** Kotlin Multiplatform 2.0.21, Compose 2024.09.03, SwiftUI (iOS 17+), Firebase Firestore, kotlin.test, coroutines 1.8.1.

Spec: `docs/superpowers/specs/2026-05-12-ingredientes-y-calculo-materia-prima-design.md`.

---

## Fase A — Modelos base (shared)

### Task A1: Crear `IngredientUnit` enum

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/model/IngredientUnit.kt`

- [ ] **Step 1: Crear archivo**

```kotlin
package me.busta.barksaccountant.model

enum class IngredientUnit(val symbol: String) {
    GRAMS("g"),
    KILOGRAMS("kg"),
    MILLILITERS("ml"),
    LITERS("l"),
    UNITS("u");

    companion object {
        fun fromName(name: String?): IngredientUnit =
            entries.firstOrNull { it.name == name } ?: GRAMS
    }
}
```

- [ ] **Step 2: Compilar shared**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/model/IngredientUnit.kt
git commit -m "feat: add IngredientUnit enum with g/kg/ml/l/u"
```

---

### Task A2: Crear `Ingredient` y `ProductIngredient`

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/model/Ingredient.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/model/ProductIngredient.kt`

- [ ] **Step 1: Crear `Ingredient.kt`**

```kotlin
package me.busta.barksaccountant.model

data class Ingredient(
    val id: String,
    val name: String,
    val unit: IngredientUnit
)
```

- [ ] **Step 2: Crear `ProductIngredient.kt`**

```kotlin
package me.busta.barksaccountant.model

data class ProductIngredient(
    val ingredientId: String,
    val ingredientName: String,
    val unit: IngredientUnit,
    val quantity: Double
)
```

- [ ] **Step 3: Compilar shared**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/model/Ingredient.kt shared/src/commonMain/kotlin/me/busta/barksaccountant/model/ProductIngredient.kt
git commit -m "feat: add Ingredient and ProductIngredient models"
```

---

### Task A3: Añadir `ingredients` a `Product`

**Files:**
- Modify: `shared/src/commonMain/kotlin/me/busta/barksaccountant/model/Product.kt`

- [ ] **Step 1: Actualizar `Product.kt`**

```kotlin
package me.busta.barksaccountant.model

data class Product(
    val id: String,
    val name: String,
    val unitPrice: Double,
    val ingredients: List<ProductIngredient> = emptyList()
)
```

- [ ] **Step 2: Compilar shared**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL (existing usages compile because the new field has a default).

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/model/Product.kt
git commit -m "feat: add optional ingredients list to Product"
```

---

## Fase B — Calculador puro + tests

### Task B1: Enable `commonTest` source set

**Files:**
- Modify: `shared/build.gradle.kts`

- [ ] **Step 1: Añadir dependencia de kotlin-test a `commonTest`**

Reemplaza el bloque `sourceSets { … }` en `shared/build.gradle.kts` para que contenga:

```kotlin
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.firebase.firestore)
        }
    }
```

- [ ] **Step 2: Verificar que el target test existe**

Run: `./gradlew :shared:tasks --all | grep -i test | head -20`
Expected: lista que incluye `iosSimulatorArm64Test` o similar (los targets de test se generan automáticamente).

- [ ] **Step 3: Commit**

```bash
git add shared/build.gradle.kts
git commit -m "chore: enable commonTest source set with kotlin-test"
```

---

### Task B2: Crear tipos de resultado del calculador

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialTypes.kt`

- [ ] **Step 1: Crear archivo**

```kotlin
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
```

- [ ] **Step 2: Compilar shared**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialTypes.kt
git commit -m "feat: add raw material calculator result types"
```

---

### Task B3: Test failing: suma simple de ingredientes (TDD)

**Files:**
- Create: `shared/src/commonTest/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialCalculatorTest.kt`

- [ ] **Step 1: Escribir test que falla**

```kotlin
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
}
```

- [ ] **Step 2: Ejecutar test — debe fallar por falta de `RawMaterialCalculator`**

Run: `./gradlew :shared:iosSimulatorArm64Test --tests "me.busta.barksaccountant.feature.purchases.calculation.RawMaterialCalculatorTest.computePlan_sumsSameIngredientAcrossProducts"`
Expected: FAIL con "unresolved reference: RawMaterialCalculator"

- [ ] **Step 3: Commit (test rojo)**

```bash
git add shared/src/commonTest/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialCalculatorTest.kt
git commit -m "test: failing test for plan ingredient summing"
```

---

### Task B4: Implementación mínima para pasar el test

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialCalculator.kt`

- [ ] **Step 1: Crear calculador**

```kotlin
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
```

- [ ] **Step 2: Ejecutar test — debe pasar**

Run: `./gradlew :shared:iosSimulatorArm64Test --tests "me.busta.barksaccountant.feature.purchases.calculation.RawMaterialCalculatorTest.computePlan_sumsSameIngredientAcrossProducts"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialCalculator.kt
git commit -m "feat: implement RawMaterialCalculator.computePlan and computeCapacity"
```

---

### Task B5: Tests adicionales del calculador

**Files:**
- Modify: `shared/src/commonTest/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialCalculatorTest.kt`

- [ ] **Step 1: Añadir tests al archivo existente**

Añade estos métodos dentro de la clase `RawMaterialCalculatorTest` (después del test existente):

```kotlin
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
        val sandia = product("p1", "Helado de sandía") // sin ingredientes
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
```

- [ ] **Step 2: Ejecutar todos los tests**

Run: `./gradlew :shared:iosSimulatorArm64Test --tests "me.busta.barksaccountant.feature.purchases.calculation.RawMaterialCalculatorTest"`
Expected: PASS (8 tests)

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonTest/kotlin/me/busta/barksaccountant/feature/purchases/calculation/RawMaterialCalculatorTest.kt
git commit -m "test: cover conversions, empty recipes, and capacity edge cases"
```

---

## Fase C — Repositorio de ingredientes (shared)

### Task C1: Interfaz `IngredientRepository`

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/data/repository/IngredientRepository.kt`

- [ ] **Step 1: Crear interfaz**

```kotlin
package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Ingredient

interface IngredientRepository {
    suspend fun getIngredients(): List<Ingredient>
    suspend fun getIngredient(id: String): Ingredient?
    suspend fun saveIngredient(ingredient: Ingredient): Ingredient
    suspend fun updateIngredient(ingredient: Ingredient): Ingredient
    suspend fun deleteIngredient(id: String)
}
```

- [ ] **Step 2: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/data/repository/IngredientRepository.kt
git commit -m "feat: add IngredientRepository interface"
```

---

### Task C2: `FirestoreIngredientRepository`

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/data/repository/FirestoreIngredientRepository.kt`

- [ ] **Step 1: Crear implementación**

```kotlin
package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.data.FirestoreService
import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.model.IngredientUnit
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FirestoreIngredientRepository(
    private val firestoreService: FirestoreService,
    private val appId: String
) : IngredientRepository {

    private val collectionPath get() = "apps/$appId/ingredients"

    override suspend fun getIngredients(): List<Ingredient> {
        return firestoreService.getDocuments(collectionPath).map { mapToIngredient(it) }
    }

    override suspend fun getIngredient(id: String): Ingredient? {
        val data = firestoreService.getDocument(collectionPath, id) ?: return null
        return mapToIngredient(data, id)
    }

    override suspend fun saveIngredient(ingredient: Ingredient): Ingredient {
        val newId = Uuid.random().toString()
        val newIngredient = ingredient.copy(id = newId)
        firestoreService.setDocument(collectionPath, newId, ingredientToMap(newIngredient))
        return newIngredient
    }

    override suspend fun updateIngredient(ingredient: Ingredient): Ingredient {
        firestoreService.setDocument(collectionPath, ingredient.id, ingredientToMap(ingredient))
        return ingredient
    }

    override suspend fun deleteIngredient(id: String) {
        firestoreService.deleteDocument(collectionPath, id)
    }

    private fun ingredientToMap(ingredient: Ingredient): Map<String, Any> {
        return mapOf(
            "name" to ingredient.name,
            "unit" to ingredient.unit.name
        )
    }

    private fun mapToIngredient(data: Map<String, Any>, overrideId: String? = null): Ingredient {
        return Ingredient(
            id = overrideId ?: (data["id"] as? String ?: ""),
            name = data["name"] as? String ?: "",
            unit = IngredientUnit.fromName(data["unit"] as? String)
        )
    }
}
```

- [ ] **Step 2: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/data/repository/FirestoreIngredientRepository.kt
git commit -m "feat: add FirestoreIngredientRepository"
```

---

### Task C3: Registrar `ingredientRepository` en `ServiceLocator`

**Files:**
- Modify: `shared/src/commonMain/kotlin/me/busta/barksaccountant/di/ServiceLocator.kt`

- [ ] **Step 1: Añadir la propiedad**

Añade una línea al bloque de propiedades de `ServiceLocator`, justo después de `productRepository`:

```kotlin
    val ingredientRepository: IngredientRepository get() = FirestoreIngredientRepository(firestoreService, appId)
```

- [ ] **Step 2: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/di/ServiceLocator.kt
git commit -m "feat: expose ingredientRepository via ServiceLocator"
```

---

### Task C4: Actualizar `FirestoreProductRepository` para serializar `ingredients`

**Files:**
- Modify: `shared/src/commonMain/kotlin/me/busta/barksaccountant/data/repository/FirestoreProductRepository.kt`

- [ ] **Step 1: Importar tipos nuevos**

Añade estos imports al principio del archivo:

```kotlin
import me.busta.barksaccountant.model.IngredientUnit
import me.busta.barksaccountant.model.ProductIngredient
```

- [ ] **Step 2: Reemplazar `productToMap`**

```kotlin
    private fun productToMap(product: Product): Map<String, Any> {
        return mapOf(
            "name" to product.name,
            "unitPrice" to product.unitPrice,
            "ingredients" to product.ingredients.map { ing ->
                mapOf<String, Any>(
                    "ingredientId" to ing.ingredientId,
                    "ingredientName" to ing.ingredientName,
                    "unit" to ing.unit.name,
                    "quantity" to ing.quantity
                )
            }
        )
    }
```

- [ ] **Step 3: Reemplazar `mapToProduct`**

```kotlin
    private fun mapToProduct(data: Map<String, Any>, overrideId: String? = null): Product {
        val ingredientsData = (data["ingredients"] as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
        return Product(
            id = overrideId ?: (data["id"] as? String ?: ""),
            name = data["name"] as? String ?: "",
            unitPrice = (data["unitPrice"] as? Number)?.toDouble() ?: 0.0,
            ingredients = ingredientsData.map { m ->
                ProductIngredient(
                    ingredientId = m["ingredientId"] as? String ?: "",
                    ingredientName = m["ingredientName"] as? String ?: "",
                    unit = IngredientUnit.fromName(m["unit"] as? String),
                    quantity = (m["quantity"] as? Number)?.toDouble() ?: 0.0
                )
            }
        )
    }
```

- [ ] **Step 4: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/data/repository/FirestoreProductRepository.kt
git commit -m "feat: serialize ingredients field in FirestoreProductRepository"
```

---

## Fase D — Stores: Ingredientes (list + form)

### Task D1: Store — lista de ingredientes

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/list/IngredientsListState.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/list/IngredientsListMessage.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/list/IngredientsListEffect.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/list/IngredientsListStore.kt`

- [ ] **Step 1: State**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.list

import me.busta.barksaccountant.model.Ingredient

data class IngredientsListState(
    val ingredients: List<Ingredient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

- [ ] **Step 2: Message**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.list

import me.busta.barksaccountant.model.Ingredient

sealed interface IngredientsListMessage {
    data object Started : IngredientsListMessage
    data class IngredientsLoaded(val ingredients: List<Ingredient>) : IngredientsListMessage
    data class ErrorOccurred(val error: String) : IngredientsListMessage
}
```

- [ ] **Step 3: Effect**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.list

sealed interface IngredientsListEffect {
    data object LoadIngredients : IngredientsListEffect
}
```

- [ ] **Step 4: Store**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.list

import me.busta.barksaccountant.data.repository.IngredientRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class IngredientsListStore(
    private val ingredientRepository: IngredientRepository
) : Store<IngredientsListState, IngredientsListMessage, IngredientsListEffect>(IngredientsListState()) {

    override fun reduce(
        state: IngredientsListState,
        message: IngredientsListMessage
    ): Next<IngredientsListState, IngredientsListEffect> {
        return when (message) {
            is IngredientsListMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                IngredientsListEffect.LoadIngredients
            )
            is IngredientsListMessage.IngredientsLoaded -> Next.just(
                state.copy(ingredients = message.ingredients, isLoading = false, error = null)
            )
            is IngredientsListMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: IngredientsListEffect) {
        when (effect) {
            is IngredientsListEffect.LoadIngredients -> {
                try {
                    val list = ingredientRepository.getIngredients()
                    dispatch(IngredientsListMessage.IngredientsLoaded(list))
                } catch (e: Exception) {
                    dispatch(IngredientsListMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
```

- [ ] **Step 5: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/list/
git commit -m "feat: add IngredientsListStore"
```

---

### Task D2: Store — form de ingrediente

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/form/IngredientFormState.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/form/IngredientFormMessage.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/form/IngredientFormEffect.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/form/IngredientFormStore.kt`

- [ ] **Step 1: State**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.model.IngredientUnit

data class IngredientFormState(
    val ingredientId: String? = null,
    val isEditing: Boolean = false,
    val name: String = "",
    val unit: IngredientUnit = IngredientUnit.GRAMS,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deleteBlockedBy: List<String> = emptyList(),
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}
```

- [ ] **Step 2: Message**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.model.IngredientUnit

sealed interface IngredientFormMessage {
    data class Started(val ingredientId: String?) : IngredientFormMessage
    data class IngredientLoaded(val ingredient: Ingredient?) : IngredientFormMessage
    data class NameChanged(val text: String) : IngredientFormMessage
    data class UnitChanged(val unit: IngredientUnit) : IngredientFormMessage
    data object SaveTapped : IngredientFormMessage
    data object SaveSuccess : IngredientFormMessage
    data object DeleteTapped : IngredientFormMessage
    data object ConfirmDelete : IngredientFormMessage
    data object DismissDelete : IngredientFormMessage
    data object DismissDeleteBlocked : IngredientFormMessage
    data class DeleteBlockedBy(val productNames: List<String>) : IngredientFormMessage
    data object DeleteSuccess : IngredientFormMessage
    data class ErrorOccurred(val error: String) : IngredientFormMessage
}
```

- [ ] **Step 3: Effect**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.model.Ingredient

sealed interface IngredientFormEffect {
    data class LoadIngredient(val ingredientId: String?) : IngredientFormEffect
    data class SaveIngredient(val ingredient: Ingredient) : IngredientFormEffect
    data class UpdateIngredient(val ingredient: Ingredient) : IngredientFormEffect
    data class DeleteIngredient(val id: String) : IngredientFormEffect
}
```

- [ ] **Step 4: Store**

```kotlin
package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.data.repository.IngredientRepository
import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class IngredientFormStore(
    private val ingredientRepository: IngredientRepository,
    private val productRepository: ProductRepository
) : Store<IngredientFormState, IngredientFormMessage, IngredientFormEffect>(IngredientFormState()) {

    override fun reduce(
        state: IngredientFormState,
        message: IngredientFormMessage
    ): Next<IngredientFormState, IngredientFormEffect> {
        return when (message) {
            is IngredientFormMessage.Started -> Next.withEffects(
                state.copy(ingredientId = message.ingredientId, isEditing = message.ingredientId != null),
                IngredientFormEffect.LoadIngredient(message.ingredientId)
            )
            is IngredientFormMessage.IngredientLoaded -> {
                val i = message.ingredient ?: return Next.just(state)
                Next.just(state.copy(name = i.name, unit = i.unit))
            }
            is IngredientFormMessage.NameChanged -> Next.just(state.copy(name = message.text))
            is IngredientFormMessage.UnitChanged -> {
                if (state.isEditing) Next.just(state) else Next.just(state.copy(unit = message.unit))
            }
            is IngredientFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val ingredient = Ingredient(
                    id = state.ingredientId ?: Uuid.random().toString(),
                    name = state.name.trim(),
                    unit = state.unit
                )
                if (state.isEditing) {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        IngredientFormEffect.UpdateIngredient(ingredient)
                    )
                } else {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        IngredientFormEffect.SaveIngredient(ingredient)
                    )
                }
            }
            is IngredientFormMessage.SaveSuccess -> Next.just(state.copy(isSaving = false, savedSuccessfully = true))
            is IngredientFormMessage.DeleteTapped -> Next.just(state.copy(showDeleteConfirm = true))
            is IngredientFormMessage.ConfirmDelete -> {
                val id = state.ingredientId ?: return Next.just(state)
                Next.withEffects(
                    state.copy(showDeleteConfirm = false, isSaving = true),
                    IngredientFormEffect.DeleteIngredient(id)
                )
            }
            is IngredientFormMessage.DismissDelete -> Next.just(state.copy(showDeleteConfirm = false))
            is IngredientFormMessage.DismissDeleteBlocked -> Next.just(state.copy(deleteBlockedBy = emptyList()))
            is IngredientFormMessage.DeleteBlockedBy -> Next.just(
                state.copy(isSaving = false, deleteBlockedBy = message.productNames)
            )
            is IngredientFormMessage.DeleteSuccess -> Next.just(state.copy(isSaving = false, deletedSuccessfully = true))
            is IngredientFormMessage.ErrorOccurred -> Next.just(state.copy(isSaving = false, error = message.error))
        }
    }

    override suspend fun handleEffect(effect: IngredientFormEffect) {
        when (effect) {
            is IngredientFormEffect.LoadIngredient -> {
                try {
                    val i = effect.ingredientId?.let { ingredientRepository.getIngredient(it) }
                    dispatch(IngredientFormMessage.IngredientLoaded(i))
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is IngredientFormEffect.SaveIngredient -> {
                try {
                    ingredientRepository.saveIngredient(effect.ingredient)
                    dispatch(IngredientFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
            is IngredientFormEffect.UpdateIngredient -> {
                try {
                    ingredientRepository.updateIngredient(effect.ingredient)
                    dispatch(IngredientFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error al actualizar"))
                }
            }
            is IngredientFormEffect.DeleteIngredient -> {
                try {
                    val products = productRepository.getProducts()
                    val usedIn = products
                        .filter { p -> p.ingredients.any { it.ingredientId == effect.id } }
                        .map { it.name }
                    if (usedIn.isNotEmpty()) {
                        dispatch(IngredientFormMessage.DeleteBlockedBy(usedIn))
                    } else {
                        ingredientRepository.deleteIngredient(effect.id)
                        dispatch(IngredientFormMessage.DeleteSuccess)
                    }
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error al eliminar"))
                }
            }
        }
    }
}
```

- [ ] **Step 5: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/ingredients/form/
git commit -m "feat: add IngredientFormStore with delete guard"
```

---

## Fase E — Producto con receta (store)

### Task E1: Ampliar `ProductFormState`

**Files:**
- Modify: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormState.kt`

- [ ] **Step 1: Reescribir `ProductFormState`**

```kotlin
package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.model.ProductIngredient

data class ProductFormState(
    val productId: String? = null,
    val isEditing: Boolean = false,
    val name: String = "",
    val price: String = "",
    val ingredients: List<ProductIngredient> = emptyList(),
    val ingredientQuantityTexts: List<String> = emptyList(),
    val availableIngredients: List<Ingredient> = emptyList(),
    val showIngredientPicker: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean
        get() {
            val basicValid = name.isNotBlank() && price.isNotBlank() &&
                price.toDoubleOrNull() != null && (price.toDoubleOrNull() ?: 0.0) > 0
            if (!basicValid) return false
            val ingredientsValid = ingredientQuantityTexts.all { text ->
                val q = text.toDoubleOrNull()
                q != null && q > 0.0
            }
            return ingredientsValid
        }
}
```

- [ ] **Step 2: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormState.kt
git commit -m "feat: extend ProductFormState with ingredients and picker"
```

---

### Task E2: Ampliar `ProductFormMessage` y `ProductFormEffect`

**Files:**
- Modify: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormMessage.kt`
- Modify: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormEffect.kt`

- [ ] **Step 1: Reescribir `ProductFormMessage.kt`**

```kotlin
package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.model.Product

sealed interface ProductFormMessage {
    data class Started(val productId: String?) : ProductFormMessage
    data class ProductLoaded(val product: Product?) : ProductFormMessage
    data class IngredientsLoaded(val ingredients: List<Ingredient>) : ProductFormMessage
    data class NameChanged(val text: String) : ProductFormMessage
    data class PriceChanged(val text: String) : ProductFormMessage
    data object AddIngredientTapped : ProductFormMessage
    data object DismissIngredientPicker : ProductFormMessage
    data class IngredientPicked(val ingredient: Ingredient) : ProductFormMessage
    data class IngredientQuantityChanged(val index: Int, val text: String) : ProductFormMessage
    data class IngredientRemoved(val index: Int) : ProductFormMessage
    data object SaveTapped : ProductFormMessage
    data object SaveSuccess : ProductFormMessage
    data object DeleteTapped : ProductFormMessage
    data object ConfirmDelete : ProductFormMessage
    data object DismissDelete : ProductFormMessage
    data object DeleteSuccess : ProductFormMessage
    data class ErrorOccurred(val error: String) : ProductFormMessage
}
```

- [ ] **Step 2: Añadir `LoadIngredients` effect**

Reescribe `ProductFormEffect.kt`:

```kotlin
package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.model.Product

sealed interface ProductFormEffect {
    data class LoadProduct(val productId: String?) : ProductFormEffect
    data object LoadIngredients : ProductFormEffect
    data class SaveProduct(val product: Product) : ProductFormEffect
    data class UpdateProduct(val product: Product) : ProductFormEffect
    data class DeleteProduct(val id: String) : ProductFormEffect
}
```

- [ ] **Step 3: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: FAIL (ProductFormStore aún no maneja los nuevos mensajes/efectos — lo arreglamos en la siguiente task).

- [ ] **Step 4: Commit (parcial, todavía sin compilar)**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormMessage.kt shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormEffect.kt
git commit -m "feat: add ingredient messages/effects to ProductForm (WIP)"
```

---

### Task E3: Actualizar `ProductFormStore` para gestionar ingredientes

**Files:**
- Modify: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormStore.kt`

- [ ] **Step 1: Reescribir archivo**

```kotlin
package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.data.repository.IngredientRepository
import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductFormStore(
    private val productRepository: ProductRepository,
    private val ingredientRepository: IngredientRepository
) : Store<ProductFormState, ProductFormMessage, ProductFormEffect>(ProductFormState()) {

    override fun reduce(state: ProductFormState, message: ProductFormMessage): Next<ProductFormState, ProductFormEffect> {
        return when (message) {
            is ProductFormMessage.Started -> Next.withEffects(
                state.copy(
                    productId = message.productId,
                    isEditing = message.productId != null
                ),
                ProductFormEffect.LoadProduct(message.productId),
                ProductFormEffect.LoadIngredients
            )
            is ProductFormMessage.ProductLoaded -> {
                val product = message.product ?: return Next.just(state)
                Next.just(
                    state.copy(
                        name = product.name,
                        price = product.unitPrice.toString(),
                        ingredients = product.ingredients,
                        ingredientQuantityTexts = product.ingredients.map { formatQty(it.quantity) }
                    )
                )
            }
            is ProductFormMessage.IngredientsLoaded -> Next.just(state.copy(availableIngredients = message.ingredients))
            is ProductFormMessage.NameChanged -> Next.just(state.copy(name = message.text))
            is ProductFormMessage.PriceChanged -> Next.just(state.copy(price = message.text))
            is ProductFormMessage.AddIngredientTapped -> Next.just(state.copy(showIngredientPicker = true))
            is ProductFormMessage.DismissIngredientPicker -> Next.just(state.copy(showIngredientPicker = false))
            is ProductFormMessage.IngredientPicked -> {
                val already = state.ingredients.any { it.ingredientId == message.ingredient.id }
                if (already) return Next.just(state.copy(showIngredientPicker = false))
                val newIng = ProductIngredient(
                    ingredientId = message.ingredient.id,
                    ingredientName = message.ingredient.name,
                    unit = message.ingredient.unit,
                    quantity = 0.0
                )
                Next.just(
                    state.copy(
                        showIngredientPicker = false,
                        ingredients = state.ingredients + newIng,
                        ingredientQuantityTexts = state.ingredientQuantityTexts + ""
                    )
                )
            }
            is ProductFormMessage.IngredientQuantityChanged -> {
                val idx = message.index
                if (idx !in state.ingredients.indices) return Next.just(state)
                val newTexts = state.ingredientQuantityTexts.toMutableList().apply { this[idx] = message.text }
                val parsed = message.text.toDoubleOrNull() ?: 0.0
                val newIngs = state.ingredients.toMutableList().apply {
                    this[idx] = this[idx].copy(quantity = parsed)
                }
                Next.just(state.copy(ingredients = newIngs, ingredientQuantityTexts = newTexts))
            }
            is ProductFormMessage.IngredientRemoved -> {
                val idx = message.index
                if (idx !in state.ingredients.indices) return Next.just(state)
                Next.just(
                    state.copy(
                        ingredients = state.ingredients.toMutableList().apply { removeAt(idx) },
                        ingredientQuantityTexts = state.ingredientQuantityTexts.toMutableList().apply { removeAt(idx) }
                    )
                )
            }
            is ProductFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val product = Product(
                    id = state.productId ?: Uuid.random().toString(),
                    name = state.name,
                    unitPrice = state.price.toDouble(),
                    ingredients = state.ingredients
                )
                if (state.isEditing) {
                    Next.withEffects(state.copy(isSaving = true, error = null), ProductFormEffect.UpdateProduct(product))
                } else {
                    Next.withEffects(state.copy(isSaving = true, error = null), ProductFormEffect.SaveProduct(product))
                }
            }
            is ProductFormMessage.SaveSuccess -> Next.just(state.copy(isSaving = false, savedSuccessfully = true))
            is ProductFormMessage.DeleteTapped -> Next.just(state.copy(showDeleteConfirm = true))
            is ProductFormMessage.ConfirmDelete -> {
                val id = state.productId ?: return Next.just(state)
                Next.withEffects(state.copy(showDeleteConfirm = false, isSaving = true), ProductFormEffect.DeleteProduct(id))
            }
            is ProductFormMessage.DismissDelete -> Next.just(state.copy(showDeleteConfirm = false))
            is ProductFormMessage.DeleteSuccess -> Next.just(state.copy(isSaving = false, deletedSuccessfully = true))
            is ProductFormMessage.ErrorOccurred -> Next.just(state.copy(isSaving = false, error = message.error))
        }
    }

    override suspend fun handleEffect(effect: ProductFormEffect) {
        when (effect) {
            is ProductFormEffect.LoadProduct -> {
                try {
                    val product = effect.productId?.let { productRepository.getProduct(it) }
                    dispatch(ProductFormMessage.ProductLoaded(product))
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is ProductFormEffect.LoadIngredients -> {
                try {
                    val list = ingredientRepository.getIngredients()
                    dispatch(ProductFormMessage.IngredientsLoaded(list))
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al cargar ingredientes"))
                }
            }
            is ProductFormEffect.SaveProduct -> {
                try {
                    productRepository.saveProduct(effect.product)
                    dispatch(ProductFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
            is ProductFormEffect.UpdateProduct -> {
                try {
                    productRepository.updateProduct(effect.product)
                    dispatch(ProductFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al actualizar"))
                }
            }
            is ProductFormEffect.DeleteProduct -> {
                try {
                    productRepository.deleteProduct(effect.id)
                    dispatch(ProductFormMessage.DeleteSuccess)
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al eliminar"))
                }
            }
        }
    }

    private fun formatQty(q: Double): String {
        return if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()
    }
}
```

- [ ] **Step 2: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/settings/products/form/ProductFormStore.kt
git commit -m "feat: handle ingredients in ProductFormStore"
```

---

## Fase F — Stores: cálculos en Compras

### Task F1: Store — cálculo directo (`productionplan`)

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productionplan/ProductionPlanState.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productionplan/ProductionPlanMessage.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productionplan/ProductionPlanEffect.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productionplan/ProductionPlanStore.kt`

- [ ] **Step 1: State (con computed `result`)**

```kotlin
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
```

- [ ] **Step 2: Message**

```kotlin
package me.busta.barksaccountant.feature.purchases.productionplan

import me.busta.barksaccountant.model.Product

sealed interface ProductionPlanMessage {
    data object Started : ProductionPlanMessage
    data class ProductsLoaded(val products: List<Product>) : ProductionPlanMessage
    data object AddProductTapped : ProductionPlanMessage
    data object DismissPicker : ProductionPlanMessage
    data class ProductPicked(val product: Product) : ProductionPlanMessage
    data class QuantityChanged(val productId: String, val text: String) : ProductionPlanMessage
    data class RowRemoved(val productId: String) : ProductionPlanMessage
    data class ErrorOccurred(val error: String) : ProductionPlanMessage
}
```

- [ ] **Step 3: Effect**

```kotlin
package me.busta.barksaccountant.feature.purchases.productionplan

sealed interface ProductionPlanEffect {
    data object LoadProducts : ProductionPlanEffect
}
```

- [ ] **Step 4: Store**

```kotlin
package me.busta.barksaccountant.feature.purchases.productionplan

import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class ProductionPlanStore(
    private val productRepository: ProductRepository
) : Store<ProductionPlanState, ProductionPlanMessage, ProductionPlanEffect>(ProductionPlanState()) {

    override fun reduce(
        state: ProductionPlanState,
        message: ProductionPlanMessage
    ): Next<ProductionPlanState, ProductionPlanEffect> {
        return when (message) {
            is ProductionPlanMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                ProductionPlanEffect.LoadProducts
            )
            is ProductionPlanMessage.ProductsLoaded -> Next.just(
                state.copy(availableProducts = message.products, isLoading = false)
            )
            is ProductionPlanMessage.AddProductTapped -> Next.just(state.copy(showProductPicker = true))
            is ProductionPlanMessage.DismissPicker -> Next.just(state.copy(showProductPicker = false))
            is ProductionPlanMessage.ProductPicked -> {
                val already = state.rows.any { it.productId == message.product.id }
                if (already) return Next.just(state.copy(showProductPicker = false))
                Next.just(
                    state.copy(
                        showProductPicker = false,
                        rows = state.rows + PlanRow(
                            productId = message.product.id,
                            productName = message.product.name,
                            quantityText = "1"
                        )
                    )
                )
            }
            is ProductionPlanMessage.QuantityChanged -> {
                Next.just(
                    state.copy(
                        rows = state.rows.map { row ->
                            if (row.productId == message.productId) row.copy(quantityText = message.text)
                            else row
                        }
                    )
                )
            }
            is ProductionPlanMessage.RowRemoved -> Next.just(
                state.copy(rows = state.rows.filterNot { it.productId == message.productId })
            )
            is ProductionPlanMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: ProductionPlanEffect) {
        when (effect) {
            is ProductionPlanEffect.LoadProducts -> {
                try {
                    val products = productRepository.getProducts()
                    dispatch(ProductionPlanMessage.ProductsLoaded(products))
                } catch (e: Exception) {
                    dispatch(ProductionPlanMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
```

- [ ] **Step 5: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productionplan/
git commit -m "feat: add ProductionPlanStore for direct raw material calculation"
```

---

### Task F2: Store — cálculo inverso (`productioncapacity`)

**Files:**
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productioncapacity/ProductionCapacityState.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productioncapacity/ProductionCapacityMessage.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productioncapacity/ProductionCapacityEffect.kt`
- Create: `shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productioncapacity/ProductionCapacityStore.kt`

- [ ] **Step 1: State**

```kotlin
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
```

- [ ] **Step 2: Message**

```kotlin
package me.busta.barksaccountant.feature.purchases.productioncapacity

import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient

sealed interface ProductionCapacityMessage {
    data object Started : ProductionCapacityMessage
    data class ProductsLoaded(val products: List<Product>) : ProductionCapacityMessage
    data object ProductPickerOpened : ProductionCapacityMessage
    data object DismissProductPicker : ProductionCapacityMessage
    data class ProductPicked(val product: Product) : ProductionCapacityMessage
    data object IngredientPickerOpened : ProductionCapacityMessage
    data object DismissIngredientPicker : ProductionCapacityMessage
    data class IngredientPicked(val ingredient: ProductIngredient) : ProductionCapacityMessage
    data class AvailableQuantityChanged(val text: String) : ProductionCapacityMessage
    data class ErrorOccurred(val error: String) : ProductionCapacityMessage
}
```

- [ ] **Step 3: Effect**

```kotlin
package me.busta.barksaccountant.feature.purchases.productioncapacity

sealed interface ProductionCapacityEffect {
    data object LoadProducts : ProductionCapacityEffect
}
```

- [ ] **Step 4: Store**

```kotlin
package me.busta.barksaccountant.feature.purchases.productioncapacity

import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class ProductionCapacityStore(
    private val productRepository: ProductRepository
) : Store<ProductionCapacityState, ProductionCapacityMessage, ProductionCapacityEffect>(ProductionCapacityState()) {

    override fun reduce(
        state: ProductionCapacityState,
        message: ProductionCapacityMessage
    ): Next<ProductionCapacityState, ProductionCapacityEffect> {
        return when (message) {
            is ProductionCapacityMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                ProductionCapacityEffect.LoadProducts
            )
            is ProductionCapacityMessage.ProductsLoaded -> Next.just(
                state.copy(availableProducts = message.products, isLoading = false)
            )
            is ProductionCapacityMessage.ProductPickerOpened -> Next.just(state.copy(showProductPicker = true))
            is ProductionCapacityMessage.DismissProductPicker -> Next.just(state.copy(showProductPicker = false))
            is ProductionCapacityMessage.ProductPicked -> Next.just(
                state.copy(
                    showProductPicker = false,
                    selectedProduct = message.product,
                    selectedIngredient = null,
                    availableQuantityText = ""
                )
            )
            is ProductionCapacityMessage.IngredientPickerOpened -> Next.just(state.copy(showIngredientPicker = true))
            is ProductionCapacityMessage.DismissIngredientPicker -> Next.just(state.copy(showIngredientPicker = false))
            is ProductionCapacityMessage.IngredientPicked -> Next.just(
                state.copy(
                    showIngredientPicker = false,
                    selectedIngredient = message.ingredient,
                    availableQuantityText = ""
                )
            )
            is ProductionCapacityMessage.AvailableQuantityChanged -> Next.just(
                state.copy(availableQuantityText = message.text)
            )
            is ProductionCapacityMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: ProductionCapacityEffect) {
        when (effect) {
            is ProductionCapacityEffect.LoadProducts -> {
                try {
                    val products = productRepository.getProducts()
                    dispatch(ProductionCapacityMessage.ProductsLoaded(products))
                } catch (e: Exception) {
                    dispatch(ProductionCapacityMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
```

- [ ] **Step 5: Compilar**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/me/busta/barksaccountant/feature/purchases/productioncapacity/
git commit -m "feat: add ProductionCapacityStore for inverse calculation"
```

---

## Fase G — Android UI

> **Nota de estilo:** el proyecto usa un sistema de diseño propio (`BarksCard`, `barksColors`, `omnesStyle`, `BarksRed`, etc.). Sigue esos mismos componentes y tokens al implementar cada pantalla. Si necesitas referenciar un archivo existente como patrón, usa `ProductFormScreen.kt` (form) y `ProductsListScreen.kt` (list).

### Task G1: Pantalla `IngredientsListScreen`

**Files:**
- Create: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/IngredientsListScreen.kt`

- [ ] **Step 1: Crear pantalla siguiendo patrón de `ProductsListScreen`**

Estructura:
- `@Composable fun IngredientsListScreen(serviceLocator, onBack, onIngredientClicked, onAddIngredient)`.
- `remember { IngredientsListStore(serviceLocator.ingredientRepository) }`.
- `LaunchedEffect(Unit) { store.dispatch(IngredientsListMessage.Started) }`.
- `DisposableEffect(Unit) { onDispose { store.dispose() } }`.
- `Scaffold` con `TopAppBar` (título "Ingredientes", botón back, action `+` que llama `onAddIngredient`).
- Si `state.isLoading` → `CircularProgressIndicator` centrado.
- Si `state.ingredients.isEmpty()` → texto "No hay ingredientes" centrado con `omnesStyle(17)` en `colors.secondaryText`.
- Si no → `LazyColumn` dentro de `BarksCard`, un `Row` por ingrediente con nombre (`omnesStyle(17, FontWeight.SemiBold)`) y a la derecha un chip con `ingredient.unit.symbol` (fondo `colors.fieldBackground`, `RoundedCornerShape(8.dp)`, padding 8/4, texto `omnesStyle(13, FontWeight.SemiBold)`).
- Cada fila es `.clickable { onIngredientClicked(ingredient.id) }`.

Código completo:

```kotlin
package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.ingredients.list.IngredientsListMessage
import me.busta.barksaccountant.feature.settings.ingredients.list.IngredientsListStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsListScreen(
    serviceLocator: ServiceLocator,
    onBack: () -> Unit,
    onIngredientClicked: (String) -> Unit,
    onAddIngredient: () -> Unit
) {
    val store = remember { IngredientsListStore(serviceLocator.ingredientRepository) }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(IngredientsListMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingredientes", style = omnesStyle(18, FontWeight.SemiBold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onAddIngredient) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.screenBackground)
            )
        },
        containerColor = colors.screenBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.ingredients.isEmpty() -> Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No hay ingredientes", style = omnesStyle(17), color = colors.secondaryText)
                }
                else -> BarksCard(colors = colors) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.ingredients.forEach { ingredient ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onIngredientClicked(ingredient.id) }.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ingredient.name, style = omnesStyle(17, FontWeight.SemiBold), color = colors.primaryText)
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(colors.fieldBackground).padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(ingredient.unit.symbol, style = omnesStyle(13, FontWeight.SemiBold), color = colors.secondaryText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Compilar Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL (pantalla aún sin ruta; solo compila).

- [ ] **Step 3: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/IngredientsListScreen.kt
git commit -m "feat(android): add IngredientsListScreen"
```

---

### Task G2: Pantalla `IngredientFormScreen`

**Files:**
- Create: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/IngredientFormScreen.kt`

- [ ] **Step 1: Crear pantalla**

Sigue el patrón de `ProductFormScreen.kt`:

- `ProductFormScreen` tiene 3 secciones (Info card, Save card, Delete card con su `AlertDialog`). Replicamos lo mismo para Ingredient, pero en vez de precio hay un selector de unidad.
- `ExposedDropdownMenuBox` para la unidad (deshabilitada si `state.isEditing`).
- Si `state.deleteBlockedBy.isNotEmpty()` → `AlertDialog` extra que lista los productos que lo usan; el botón "Aceptar" despacha `IngredientFormMessage.DismissDeleteBlocked`.

Código completo:

```kotlin
package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.ingredients.form.IngredientFormMessage
import me.busta.barksaccountant.feature.settings.ingredients.form.IngredientFormStore
import me.busta.barksaccountant.model.IngredientUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientFormScreen(
    serviceLocator: ServiceLocator,
    ingredientId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember {
        IngredientFormStore(
            ingredientRepository = serviceLocator.ingredientRepository,
            productRepository = serviceLocator.productRepository
        )
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(IngredientFormMessage.Started(ingredientId)) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    LaunchedEffect(state.savedSuccessfully) { if (state.savedSuccessfully) onSaved() }
    LaunchedEffect(state.deletedSuccessfully) { if (state.deletedSuccessfully) onSaved() }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { store.dispatch(IngredientFormMessage.DismissDelete) },
            title = { Text("Eliminar ingrediente") },
            text = { Text("¿Estás seguro de que quieres eliminar este ingrediente?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(IngredientFormMessage.ConfirmDelete) }) {
                    Text("Eliminar", color = BarksRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(IngredientFormMessage.DismissDelete) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (state.deleteBlockedBy.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { store.dispatch(IngredientFormMessage.DismissDeleteBlocked) },
            title = { Text("No se puede eliminar") },
            text = { Text("Este ingrediente se usa en: ${state.deleteBlockedBy.joinToString(", ")}. Elimínalo de esos productos primero.") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(IngredientFormMessage.DismissDeleteBlocked) }) { Text("Aceptar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Editar Ingrediente" else "Nuevo Ingrediente",
                        style = omnesStyle(18, FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.screenBackground)
            )
        },
        containerColor = colors.screenBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BarksCard(title = "Información", colors = colors) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Name field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nombre", style = omnesStyle(13), color = colors.secondaryText)
                        BasicTextField(
                            value = state.name,
                            onValueChange = { store.dispatch(IngredientFormMessage.NameChanged(it)) },
                            textStyle = omnesStyle(17, FontWeight.SemiBold).copy(color = colors.primaryText),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                                .background(colors.fieldBackground)
                                .border(1.dp, if (state.name.isEmpty()) BarksRed.copy(alpha = 0.45f) else colors.fieldBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                    if (state.name.isEmpty()) {
                                        Text("Ej: Pollo", style = omnesStyle(17, FontWeight.SemiBold), color = colors.secondaryText.copy(alpha = 0.5f))
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                    // Unit dropdown
                    var expanded by remember { mutableStateOf(false) }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Unidad", style = omnesStyle(13), color = colors.secondaryText)
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (!state.isEditing) expanded = !expanded }) {
                            Row(
                                modifier = Modifier.menuAnchor().fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                                    .background(colors.fieldBackground)
                                    .border(1.dp, colors.fieldBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${state.unit.symbol} — ${unitLabel(state.unit)}", style = omnesStyle(17, FontWeight.SemiBold), color = colors.primaryText)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.secondaryText)
                            }
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                IngredientUnit.entries.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text("${u.symbol} — ${unitLabel(u)}", style = omnesStyle(15)) },
                                        onClick = {
                                            store.dispatch(IngredientFormMessage.UnitChanged(u))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        if (state.isEditing) {
                            Text("La unidad no se puede cambiar", style = omnesStyle(12), color = colors.secondaryText)
                        }
                    }
                }
            }

            // Save Card
            BarksCard(colors = colors) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (state.canSave && !state.isSaving) BarksRed else BarksRed.copy(alpha = 0.6f))
                            .clickable(enabled = state.canSave && !state.isSaving) {
                                store.dispatch(IngredientFormMessage.SaveTapped)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = BarksWhite, strokeWidth = 2.dp)
                        } else {
                            Text("Guardar", style = omnesStyle(16, FontWeight.SemiBold), color = BarksWhite)
                        }
                    }
                    state.error?.let { Text(it, style = omnesStyle(13), color = BarksRed) }
                }
            }

            if (state.isEditing) {
                BarksCard(colors = colors) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, BarksRed, RoundedCornerShape(14.dp))
                            .clickable { store.dispatch(IngredientFormMessage.DeleteTapped) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Eliminar ingrediente", style = omnesStyle(16, FontWeight.SemiBold), color = BarksRed)
                    }
                }
            }
        }
    }
}

private fun unitLabel(u: IngredientUnit): String = when (u) {
    IngredientUnit.GRAMS -> "gramos"
    IngredientUnit.KILOGRAMS -> "kilos"
    IngredientUnit.MILLILITERS -> "mililitros"
    IngredientUnit.LITERS -> "litros"
    IngredientUnit.UNITS -> "unidades"
}
```

- [ ] **Step 2: Compilar Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/IngredientFormScreen.kt
git commit -m "feat(android): add IngredientFormScreen"
```

---

### Task G3: Rutas de Ingredientes en `MainScreen` + enlace en `SettingsScreen`

**Files:**
- Modify: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/MainScreen.kt`
- Modify: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/SettingsScreen.kt`

- [ ] **Step 1: Añadir rutas en el `NavHost` de `MainScreen`**

Busca el bloque del tab Settings en `MainScreen.kt` (donde están las rutas `settings`, `products_list`, `product_form?...`) y añade dentro del mismo grafo:

```kotlin
composable("ingredients_list") {
    IngredientsListScreen(
        serviceLocator = serviceLocator,
        onBack = { navController.popBackStack() },
        onIngredientClicked = { id -> navController.navigate("ingredient_form?ingredientId=$id") },
        onAddIngredient = { navController.navigate("ingredient_form") }
    )
}
composable(
    route = "ingredient_form?ingredientId={ingredientId}",
    arguments = listOf(navArgument("ingredientId") { type = NavType.StringType; nullable = true; defaultValue = null })
) { backStackEntry ->
    val ingredientId = backStackEntry.arguments?.getString("ingredientId")
    IngredientFormScreen(
        serviceLocator = serviceLocator,
        ingredientId = ingredientId,
        onSaved = { navController.popBackStack() },
        onBack = { navController.popBackStack() }
    )
}
```

Imports necesarios:
```kotlin
import me.busta.barksaccountant.android.ui.screen.settings.IngredientsListScreen
import me.busta.barksaccountant.android.ui.screen.settings.IngredientFormScreen
```

- [ ] **Step 2: Añadir entrada en `SettingsScreen`**

Donde `SettingsScreen.kt` tiene los items de navegación "Productos" y "Clientes", añade uno "Ingredientes" entre ambos que llame `onIngredientsClicked()`. Añade el parámetro `onIngredientsClicked: () -> Unit` al `@Composable fun SettingsScreen(...)` y pásalo desde `MainScreen` como `{ navController.navigate("ingredients_list") }`. Sigue exactamente el mismo patrón visual que los items de Productos y Clientes.

- [ ] **Step 3: Compilar Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Verificar manualmente** (opcional si tienes emulador)

Corre la app, navega a Settings, toca "Ingredientes", crea uno, guárdalo, entra en la lista y confirma que aparece.

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/MainScreen.kt androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/SettingsScreen.kt
git commit -m "feat(android): wire ingredients navigation in Settings"
```

---

### Task G4: Picker de ingredientes (Android)

**Files:**
- Create: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/IngredientPickerDialog.kt`

- [ ] **Step 1: Crear dialog reutilizable**

```kotlin
package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.model.Ingredient

@Composable
fun IngredientPickerDialog(
    ingredients: List<Ingredient>,
    onSelected: (Ingredient) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = barksColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elegir ingrediente", style = omnesStyle(17, FontWeight.SemiBold)) },
        text = {
            if (ingredients.isEmpty()) {
                Text("No hay ingredientes. Créalos en Settings → Ingredientes.", style = omnesStyle(14), color = colors.secondaryText)
            } else {
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(ingredients, key = { it.id }) { i ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onSelected(i) }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(i.name, style = omnesStyle(16, FontWeight.SemiBold), color = colors.primaryText)
                            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(colors.fieldBackground).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(i.unit.symbol, style = omnesStyle(12, FontWeight.SemiBold), color = colors.secondaryText)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
```

- [ ] **Step 2: Compilar**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/IngredientPickerDialog.kt
git commit -m "feat(android): add IngredientPickerDialog"
```

---

### Task G5: Integrar ingredientes en `ProductFormScreen` (Android)

**Files:**
- Modify: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/ProductFormScreen.kt`

- [ ] **Step 1: Actualizar creación del store**

Reemplaza:
```kotlin
val store = remember { ProductFormStore(productRepository = serviceLocator.productRepository) }
```
por:
```kotlin
val store = remember {
    ProductFormStore(
        productRepository = serviceLocator.productRepository,
        ingredientRepository = serviceLocator.ingredientRepository
    )
}
```

- [ ] **Step 2: Añadir picker + sección de ingredientes**

Debajo del `InfoCard(...)` y antes del `SaveCard(...)` en el `Column` del `Scaffold`, añade una nueva card "Ingredientes". Y antes del `Scaffold` gestiona el picker:

```kotlin
if (state.showIngredientPicker) {
    IngredientPickerDialog(
        ingredients = state.availableIngredients.filter { available ->
            state.ingredients.none { it.ingredientId == available.id }
        },
        onSelected = { ing -> store.dispatch(ProductFormMessage.IngredientPicked(ing)) },
        onDismiss = { store.dispatch(ProductFormMessage.DismissIngredientPicker) }
    )
}
```

Nuevo composable `IngredientsCard` dentro del mismo archivo (reemplazando nada, añadir al final junto a los otros privates):

```kotlin
@Composable
private fun IngredientsCard(
    ingredients: List<me.busta.barksaccountant.model.ProductIngredient>,
    quantityTexts: List<String>,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    catalogEmpty: Boolean,
    onAdd: () -> Unit,
    onQuantityChange: (Int, String) -> Unit,
    onRemove: (Int) -> Unit
) {
    BarksCard(title = "Ingredientes", colors = colors) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (ingredients.isEmpty()) {
                Text("No hay ingredientes añadidos", style = omnesStyle(14), color = colors.secondaryText)
            } else {
                ingredients.forEachIndexed { index, ing ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(ing.ingredientName, style = omnesStyle(15, FontWeight.SemiBold), color = colors.primaryText, modifier = Modifier.weight(1f))
                        BasicTextField(
                            value = quantityTexts.getOrNull(index) ?: "",
                            onValueChange = { onQuantityChange(index, it) },
                            textStyle = omnesStyle(15, FontWeight.SemiBold).copy(color = colors.primaryText),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.width(80.dp).height(36.dp).clip(RoundedCornerShape(8.dp))
                                .background(colors.fieldBackground)
                                .border(1.dp, colors.fieldBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            decorationBox = { inner -> Box(contentAlignment = Alignment.CenterStart) { inner() } }
                        )
                        Text(ing.unit.symbol, style = omnesStyle(13, FontWeight.SemiBold), color = colors.secondaryText, modifier = Modifier.width(28.dp))
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Quitar", tint = BarksRed)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BarksRed, RoundedCornerShape(12.dp))
                    .clickable(enabled = !catalogEmpty, onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Text(if (catalogEmpty) "No hay ingredientes en el catálogo" else "+ Añadir ingrediente", style = omnesStyle(14, FontWeight.SemiBold), color = BarksRed)
            }
            if (catalogEmpty) {
                Text("Créalos en Settings → Ingredientes", style = omnesStyle(12), color = colors.secondaryText)
            }
        }
    }
}
```

Y usalo en el `Column` principal (entre `InfoCard` y `SaveCard`):

```kotlin
IngredientsCard(
    ingredients = state.ingredients,
    quantityTexts = state.ingredientQuantityTexts,
    colors = colors,
    catalogEmpty = state.availableIngredients.isEmpty(),
    onAdd = { store.dispatch(ProductFormMessage.AddIngredientTapped) },
    onQuantityChange = { i, t -> store.dispatch(ProductFormMessage.IngredientQuantityChanged(i, t)) },
    onRemove = { i -> store.dispatch(ProductFormMessage.IngredientRemoved(i)) }
)
```

Imports a añadir:
```kotlin
import androidx.compose.material.icons.filled.Delete
import me.busta.barksaccountant.model.ProductIngredient
```

- [ ] **Step 2: Compilar Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/settings/ProductFormScreen.kt
git commit -m "feat(android): add ingredients section to ProductFormScreen"
```

---

### Task G6: Pantalla `ProductionPlanScreen` (Android)

**Files:**
- Create: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/purchases/ProductionPlanScreen.kt`

- [ ] **Step 1: Crear pantalla**

Reutiliza el patrón `ProductFormScreen` + `ProductPickerDialog`. Al pulsar `+ Añadir helado` abre `ProductPickerDialog` existente. Muestra cada fila (nombre + input cantidad + papelera) con los estilos `BarksCard`/`omnesStyle`/`BarksRed`. Al final, si `state.result.needs.isNotEmpty()`, dibuja `BarksCard(title = "Materia prima necesaria")` con cada `RawMaterialNeed`: nombre a la izquierda y `formatQty(displayQuantity) + displayUnit.symbol` a la derecha. Si `state.result.productsWithoutRecipe.isNotEmpty()`, dibuja `BarksCard(title = "Productos sin receta")` con los nombres en color `colors.secondaryText`.

Formateador inline:
```kotlin
private fun formatQty(q: Double): String {
    return if (q == q.toLong().toDouble()) q.toLong().toString()
    else ((q * 100).toLong() / 100.0).toString()
}
```

Código completo:

```kotlin
package me.busta.barksaccountant.android.ui.screen.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.screen.sales.ProductPickerDialog
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.purchases.productionplan.ProductionPlanMessage
import me.busta.barksaccountant.feature.purchases.productionplan.ProductionPlanStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionPlanScreen(
    serviceLocator: ServiceLocator,
    onBack: () -> Unit
) {
    val store = remember { ProductionPlanStore(serviceLocator.productRepository) }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(ProductionPlanMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    if (state.showProductPicker) {
        ProductPickerDialog(
            products = state.availableProducts.filter { p -> state.rows.none { it.productId == p.id } },
            onSelected = { p -> store.dispatch(ProductionPlanMessage.ProductPicked(p)) },
            onDismiss = { store.dispatch(ProductionPlanMessage.DismissPicker) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planificar producción", style = omnesStyle(18, FontWeight.SemiBold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.screenBackground)
            )
        },
        containerColor = colors.screenBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BarksCard(title = "Helados a producir", colors = colors) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.rows.isEmpty()) {
                        Text("Añade helados para ver la materia prima necesaria", style = omnesStyle(14), color = colors.secondaryText)
                    }
                    state.rows.forEach { row ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(row.productName, style = omnesStyle(15, FontWeight.SemiBold), color = colors.primaryText, modifier = Modifier.weight(1f))
                            BasicTextField(
                                value = row.quantityText,
                                onValueChange = { store.dispatch(ProductionPlanMessage.QuantityChanged(row.productId, it)) },
                                textStyle = omnesStyle(15, FontWeight.SemiBold).copy(color = colors.primaryText),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(60.dp).height(36.dp).clip(RoundedCornerShape(8.dp))
                                    .background(colors.fieldBackground).border(1.dp, colors.fieldBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp),
                                decorationBox = { inner -> Box(contentAlignment = Alignment.CenterStart) { inner() } }
                            )
                            IconButton(onClick = { store.dispatch(ProductionPlanMessage.RowRemoved(row.productId)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = BarksRed)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BarksRed, RoundedCornerShape(12.dp))
                            .clickable { store.dispatch(ProductionPlanMessage.AddProductTapped) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+ Añadir helado", style = omnesStyle(14, FontWeight.SemiBold), color = BarksRed)
                    }
                }
            }

            val result = state.result
            if (result.needs.isNotEmpty()) {
                BarksCard(title = "Materia prima necesaria", colors = colors) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.needs.forEach { need ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(need.ingredientName, style = omnesStyle(15, FontWeight.SemiBold), color = colors.primaryText)
                                Text("${formatQty(need.displayQuantity)} ${need.displayUnit.symbol}", style = omnesStyle(15, FontWeight.SemiBold), color = colors.primaryText)
                            }
                        }
                    }
                }
            }
            if (result.productsWithoutRecipe.isNotEmpty()) {
                BarksCard(title = "Productos sin receta", colors = colors) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        result.productsWithoutRecipe.forEach { name ->
                            Text(name, style = omnesStyle(14), color = colors.secondaryText)
                        }
                        Text("No se incluyen en el cálculo", style = omnesStyle(12), color = colors.secondaryText)
                    }
                }
            }
        }
    }
}

private fun formatQty(q: Double): String {
    return if (q == q.toLong().toDouble()) q.toLong().toString()
    else ((q * 100).toLong() / 100.0).toString()
}
```

- [ ] **Step 2: Compilar Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL (la ruta aún no está enganchada en `MainScreen`, eso es G8).

- [ ] **Step 3: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/purchases/ProductionPlanScreen.kt
git commit -m "feat(android): add ProductionPlanScreen"
```

---

### Task G7: Pantalla `ProductionCapacityScreen` (Android)

**Files:**
- Create: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/purchases/ProductionCapacityScreen.kt`

- [ ] **Step 1: Crear pantalla**

Estructura: tres campos en columna (Helado / Ingrediente / Tengo) cada uno como `BarksCard`, y al final `BarksCard("Resultado")` si `state.result != null`.

Código completo:

```kotlin
package me.busta.barksaccountant.android.ui.screen.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.screen.sales.ProductPickerDialog
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.purchases.productioncapacity.ProductionCapacityMessage
import me.busta.barksaccountant.feature.purchases.productioncapacity.ProductionCapacityStore
import me.busta.barksaccountant.model.ProductIngredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionCapacityScreen(
    serviceLocator: ServiceLocator,
    onBack: () -> Unit
) {
    val store = remember { ProductionCapacityStore(serviceLocator.productRepository) }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(ProductionCapacityMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    if (state.showProductPicker) {
        ProductPickerDialog(
            products = state.availableProducts,
            onSelected = { p -> store.dispatch(ProductionCapacityMessage.ProductPicked(p)) },
            onDismiss = { store.dispatch(ProductionCapacityMessage.DismissProductPicker) }
        )
    }

    if (state.showIngredientPicker) {
        val recipeIngredients = state.selectedProduct?.ingredients ?: emptyList()
        AlertDialog(
            onDismissRequest = { store.dispatch(ProductionCapacityMessage.DismissIngredientPicker) },
            title = { Text("Elegir ingrediente") },
            text = {
                Column {
                    recipeIngredients.forEach { i ->
                        Row(
                            Modifier.fillMaxWidth().clickable { store.dispatch(ProductionCapacityMessage.IngredientPicked(i)) }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(i.ingredientName, style = omnesStyle(16, FontWeight.SemiBold), color = colors.primaryText)
                            Text(i.unit.symbol, style = omnesStyle(13, FontWeight.SemiBold), color = colors.secondaryText)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { store.dispatch(ProductionCapacityMessage.DismissIngredientPicker) }) { Text("Cerrar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("¿Qué puedo producir?", style = omnesStyle(18, FontWeight.SemiBold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.screenBackground)
            )
        },
        containerColor = colors.screenBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BarksCard(title = "1. Helado", colors = colors) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(colors.fieldBackground)
                        .clickable { store.dispatch(ProductionCapacityMessage.ProductPickerOpened) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        state.selectedProduct?.name ?: "Selecciona un helado",
                        style = omnesStyle(15, FontWeight.SemiBold),
                        color = if (state.selectedProduct != null) colors.primaryText else colors.secondaryText
                    )
                }
            }

            val product = state.selectedProduct
            val ingredientPickerEnabled = product != null && product.ingredients.isNotEmpty()
            BarksCard(title = "2. Ingrediente", colors = colors) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(colors.fieldBackground)
                        .clickable(enabled = ingredientPickerEnabled) { store.dispatch(ProductionCapacityMessage.IngredientPickerOpened) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        when {
                            product == null -> "Selecciona primero un helado"
                            product.ingredients.isEmpty() -> "Este helado no tiene receta definida"
                            state.selectedIngredient == null -> "Elige un ingrediente"
                            else -> "${state.selectedIngredient!!.ingredientName} (${state.selectedIngredient!!.unit.symbol})"
                        },
                        style = omnesStyle(15, FontWeight.SemiBold),
                        color = if (state.selectedIngredient != null) colors.primaryText else colors.secondaryText
                    )
                }
            }

            val ingredient: ProductIngredient? = state.selectedIngredient
            BarksCard(title = "3. Tengo", colors = colors) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicTextField(
                        value = state.availableQuantityText,
                        onValueChange = { store.dispatch(ProductionCapacityMessage.AvailableQuantityChanged(it)) },
                        enabled = ingredient != null,
                        textStyle = omnesStyle(15, FontWeight.SemiBold).copy(color = colors.primaryText),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(colors.fieldBackground).border(1.dp, colors.fieldBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (state.availableQuantityText.isEmpty()) {
                                    Text("Cantidad", style = omnesStyle(15), color = colors.secondaryText.copy(alpha = 0.5f))
                                }
                                inner()
                            }
                        }
                    )
                    Text(ingredient?.unit?.symbol ?: "", style = omnesStyle(15, FontWeight.SemiBold), color = colors.secondaryText)
                }
            }

            state.result?.let { result ->
                BarksCard(title = "Resultado", colors = colors) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            if (result.productCount < 1.0) "No alcanza ni para un helado"
                            else "Puedes hacer ${formatCapacity(result.productCount)} helados de ${state.selectedProduct?.name ?: ""}",
                            style = omnesStyle(17, FontWeight.SemiBold),
                            color = colors.primaryText
                        )
                        if (result.productCount >= 1.0 && result.otherIngredientsNeeded.isNotEmpty()) {
                            Text("Para esa cantidad necesitarás además:", style = omnesStyle(13), color = colors.secondaryText)
                            result.otherIngredientsNeeded.forEach { need ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(need.ingredientName, style = omnesStyle(15), color = colors.primaryText)
                                    Text("${formatCapacity(need.displayQuantity)} ${need.displayUnit.symbol}", style = omnesStyle(15), color = colors.primaryText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCapacity(q: Double): String {
    if (q == q.toLong().toDouble()) return q.toLong().toString()
    val rounded = ((q * 10).toLong()) / 10.0
    return rounded.toString()
}
```

- [ ] **Step 2: Compilar Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/purchases/ProductionCapacityScreen.kt
git commit -m "feat(android): add ProductionCapacityScreen"
```

---

### Task G8: Enganchar rutas de cálculos en Compras (Android)

**Files:**
- Modify: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/MainScreen.kt`
- Modify: `androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/purchases/PurchasesListScreen.kt`

- [ ] **Step 1: Añadir rutas en el grafo de Compras**

En `MainScreen.kt`, dentro del bloque del tab Compras del `NavHost`, añade:

```kotlin
composable("production_plan") {
    ProductionPlanScreen(
        serviceLocator = serviceLocator,
        onBack = { navController.popBackStack() }
    )
}
composable("production_capacity") {
    ProductionCapacityScreen(
        serviceLocator = serviceLocator,
        onBack = { navController.popBackStack() }
    )
}
```

Imports:
```kotlin
import me.busta.barksaccountant.android.ui.screen.purchases.ProductionCapacityScreen
import me.busta.barksaccountant.android.ui.screen.purchases.ProductionPlanScreen
```

- [ ] **Step 2: Añadir dos accesos arriba de `PurchasesListScreen`**

Añade dos parámetros al `@Composable fun PurchasesListScreen(...)`:
```kotlin
onOpenProductionPlan: () -> Unit,
onOpenProductionCapacity: () -> Unit,
```

Justo antes del `LazyColumn` de compras (dentro del `Column` ya existente), inserta dos `BarksCard` cliqueables con títulos "Planificar producción" y "¿Qué puedo producir?" + icono `Icons.AutoMirrored.Filled.KeyboardArrowRight`. Usa el mismo patrón visual que los items de `SettingsScreen`.

En `MainScreen.kt`, pásales los callbacks:
```kotlin
onOpenProductionPlan = { navController.navigate("production_plan") },
onOpenProductionCapacity = { navController.navigate("production_capacity") }
```

- [ ] **Step 3: Compilar Android**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Verificar manualmente** (opcional con emulador)

Abre Compras → tap "Planificar producción" → añade un helado → verifica materia prima. Vuelve y prueba "¿Qué puedo producir?".

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/MainScreen.kt androidApp/src/main/java/me/busta/barksaccountant/android/ui/screen/purchases/PurchasesListScreen.kt
git commit -m "feat(android): wire production plan/capacity into Compras tab"
```

---

## Fase H — iOS UI

> **Patrón iOS obligatorio:** cada `View` tiene su `*StoreWrapper` (`@Observable`) que usa `FlowCollector<State>` para sincronizar estado, expone `start()`, métodos de acción y `deinit { collector?.close(); store.dispose() }`. Sigue `ProductFormView.swift` + `ProductFormStoreWrapper.swift` como plantilla para todo lo que crearás aquí. Cuando lances un método Kotlin, recuerda que: `data object Foo : Msg` → `XxxMessageFoo.shared`; `data class Bar(val x: String)` → `XxxMessageBar(x: "...")`; `List<T>` → `as? [T] ?? []`.

### Task H1: `IngredientsListStoreWrapper` + `IngredientsListView`

**Files:**
- Create: `iosApp/BarksAccountantApp/Settings/IngredientsListStoreWrapper.swift`
- Create: `iosApp/BarksAccountantApp/Settings/IngredientsListView.swift`

- [ ] **Step 1: Wrapper**

```swift
import Foundation
import Shared

@Observable
final class IngredientsListStoreWrapper {
    private(set) var ingredients: [Ingredient] = []
    private(set) var isLoading: Bool = false
    private(set) var error: String?

    private let store: IngredientsListStore
    private var collector: FlowCollector<IngredientsListState>?

    init(ingredientRepository: IngredientRepository) {
        self.store = IngredientsListStore(ingredientRepository: ingredientRepository)
    }

    func start() {
        collector = FlowCollector<IngredientsListState>(flow: store.state) { [weak self] state in
            guard let self else { return }
            self.ingredients = (state.ingredients as? [Ingredient]) ?? []
            self.isLoading = state.isLoading
            self.error = state.error
        }
        store.dispatch(message: IngredientsListMessageStarted.shared)
    }

    func reload() { store.dispatch(message: IngredientsListMessageStarted.shared) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
```

- [ ] **Step 2: View**

```swift
import SwiftUI
import Shared

enum IngredientDestination: Hashable {
    case form(id: String?)
}

struct IngredientsListView: View {
    let serviceLocator: ServiceLocator
    @State private var store: IngredientsListStoreWrapper

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: IngredientsListStoreWrapper(
            ingredientRepository: serviceLocator.ingredientRepository
        ))
    }

    var body: some View {
        List {
            if store.isLoading {
                ProgressView()
            } else if store.ingredients.isEmpty {
                Text("No hay ingredientes")
                    .foregroundStyle(.secondary)
            } else {
                ForEach(store.ingredients, id: \.id) { ingredient in
                    NavigationLink(value: IngredientDestination.form(id: ingredient.id)) {
                        HStack {
                            Text(ingredient.name).fontWeight(.semibold)
                            Spacer()
                            Text(ingredient.unit.symbol)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.gray.opacity(0.15))
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }
                    }
                }
            }
        }
        .navigationTitle("Ingredientes")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink(value: IngredientDestination.form(id: nil)) {
                    Image(systemName: "plus")
                }
            }
        }
        .navigationDestination(for: IngredientDestination.self) { dest in
            switch dest {
            case .form(let id):
                IngredientFormView(serviceLocator: serviceLocator, ingredientId: id) {
                    store.reload()
                }
            }
        }
        .onAppear { store.start() }
    }
}
```

- [ ] **Step 3: Añadir entradas al `project.pbxproj`**

Para cada archivo `.swift` nuevo, añade entradas en 4 secciones:
1. `PBXBuildFile`
2. `PBXFileReference`
3. `PBXGroup` → `children` del grupo Settings (ID `D10006`)
4. `PBXSourcesBuildPhase` → `files`

Usa IDs incrementales a partir del último usado (si el último es `A10052`/`B10052`, usa `A10053`/`B10053` para el primer archivo, `A10054`/`B10054` para el segundo, etc.). Inspecciona el `project.pbxproj` con `grep -n "A1005" iosApp/BarksAccountantApp.xcodeproj/project.pbxproj | tail -10` para encontrar el último par usado.

Plantilla por archivo (sustituye `A10053`/`B10053` por los IDs que correspondan):
```
/* PBXBuildFile */
B10053 /* IngredientsListStoreWrapper.swift in Sources */ = {isa = PBXBuildFile; fileRef = A10053 /* IngredientsListStoreWrapper.swift */; };
/* PBXFileReference */
A10053 /* IngredientsListStoreWrapper.swift */ = {isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = IngredientsListStoreWrapper.swift; sourceTree = "<group>"; };
/* en children de D10006 */
A10053 /* IngredientsListStoreWrapper.swift */,
/* en files de PBXSourcesBuildPhase */
B10053 /* IngredientsListStoreWrapper.swift in Sources */,
```

- [ ] **Step 4: Compilar iOS**

Run:
```bash
cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build
```
Expected: BUILD SUCCEEDED (la view todavía no está navegable desde Settings — lo añadimos en H3).

- [ ] **Step 5: Commit**

```bash
git add iosApp/BarksAccountantApp/Settings/IngredientsListStoreWrapper.swift iosApp/BarksAccountantApp/Settings/IngredientsListView.swift iosApp/BarksAccountantApp.xcodeproj/project.pbxproj
git commit -m "feat(ios): add IngredientsListView"
```

---

### Task H2: `IngredientFormStoreWrapper` + `IngredientFormView`

**Files:**
- Create: `iosApp/BarksAccountantApp/Settings/IngredientFormStoreWrapper.swift`
- Create: `iosApp/BarksAccountantApp/Settings/IngredientFormView.swift`

- [ ] **Step 1: Wrapper**

```swift
import Foundation
import Shared

@Observable
final class IngredientFormStoreWrapper {
    private(set) var isEditing: Bool = false
    var name: String = ""
    private(set) var unit: IngredientUnit = .grams
    private(set) var isSaving: Bool = false
    private(set) var savedSuccessfully: Bool = false
    private(set) var showDeleteConfirm: Bool = false
    private(set) var deleteBlockedBy: [String] = []
    private(set) var deletedSuccessfully: Bool = false
    private(set) var canSave: Bool = false
    private(set) var error: String?

    private let store: IngredientFormStore
    private var collector: FlowCollector<IngredientFormState>?

    init(ingredientRepository: IngredientRepository, productRepository: ProductRepository) {
        self.store = IngredientFormStore(
            ingredientRepository: ingredientRepository,
            productRepository: productRepository
        )
    }

    func start(ingredientId: String?) {
        collector = FlowCollector<IngredientFormState>(flow: store.state) { [weak self] state in
            guard let self else { return }
            self.isEditing = state.isEditing
            self.name = state.name
            self.unit = state.unit
            self.isSaving = state.isSaving
            self.savedSuccessfully = state.savedSuccessfully
            self.showDeleteConfirm = state.showDeleteConfirm
            self.deleteBlockedBy = (state.deleteBlockedBy as? [String]) ?? []
            self.deletedSuccessfully = state.deletedSuccessfully
            self.canSave = state.canSave
            self.error = state.error
        }
        store.dispatch(message: IngredientFormMessageStarted(ingredientId: ingredientId))
    }

    func onNameChange(_ text: String) {
        store.dispatch(message: IngredientFormMessageNameChanged(text: text))
    }
    func onUnitChange(_ u: IngredientUnit) {
        store.dispatch(message: IngredientFormMessageUnitChanged(unit: u))
    }
    func save() { store.dispatch(message: IngredientFormMessageSaveTapped.shared) }
    func tapDelete() { store.dispatch(message: IngredientFormMessageDeleteTapped.shared) }
    func confirmDelete() { store.dispatch(message: IngredientFormMessageConfirmDelete.shared) }
    func dismissDelete() { store.dispatch(message: IngredientFormMessageDismissDelete.shared) }
    func dismissDeleteBlocked() { store.dispatch(message: IngredientFormMessageDismissDeleteBlocked.shared) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
```

- [ ] **Step 2: View**

```swift
import SwiftUI
import Shared

struct IngredientFormView: View {
    let serviceLocator: ServiceLocator
    let ingredientId: String?
    let onSaved: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var store: IngredientFormStoreWrapper

    init(serviceLocator: ServiceLocator, ingredientId: String?, onSaved: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.ingredientId = ingredientId
        self.onSaved = onSaved
        _store = State(initialValue: IngredientFormStoreWrapper(
            ingredientRepository: serviceLocator.ingredientRepository,
            productRepository: serviceLocator.productRepository
        ))
    }

    var body: some View {
        Form {
            Section("Información") {
                TextField("Nombre", text: Binding(get: { store.name }, set: store.onNameChange))
                Picker("Unidad", selection: Binding(get: { store.unit }, set: store.onUnitChange)) {
                    ForEach(Array(IngredientUnit.allCases), id: \.self) { u in
                        Text("\(u.symbol) — \(unitLabel(u))").tag(u)
                    }
                }
                .disabled(store.isEditing)
                if store.isEditing {
                    Text("La unidad no se puede cambiar").font(.caption).foregroundStyle(.secondary)
                }
            }

            Section {
                Button(action: { store.save() }) {
                    if store.isSaving { ProgressView() } else { Text("Guardar").frame(maxWidth: .infinity) }
                }
                .disabled(!store.canSave || store.isSaving)
                if let err = store.error { Text(err).foregroundStyle(.red).font(.caption) }
            }

            if store.isEditing {
                Section {
                    Button("Eliminar ingrediente", role: .destructive) { store.tapDelete() }
                }
            }
        }
        .navigationTitle(store.isEditing ? "Editar Ingrediente" : "Nuevo Ingrediente")
        .onAppear { store.start(ingredientId: ingredientId) }
        .onChange(of: store.savedSuccessfully) { _, saved in if saved { onSaved(); dismiss() } }
        .onChange(of: store.deletedSuccessfully) { _, deleted in if deleted { onSaved(); dismiss() } }
        .alert("Eliminar ingrediente", isPresented: Binding(get: { store.showDeleteConfirm }, set: { _ in store.dismissDelete() })) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        } message: { Text("¿Estás seguro?") }
        .alert("No se puede eliminar", isPresented: Binding(get: { !store.deleteBlockedBy.isEmpty }, set: { _ in store.dismissDeleteBlocked() })) {
            Button("Aceptar") { store.dismissDeleteBlocked() }
        } message: {
            Text("Este ingrediente se usa en: \(store.deleteBlockedBy.joined(separator: ", ")). Elimínalo de esos productos primero.")
        }
    }

    private func unitLabel(_ u: IngredientUnit) -> String {
        switch u {
        case .grams: return "gramos"
        case .kilograms: return "kilos"
        case .milliliters: return "mililitros"
        case .liters: return "litros"
        case .units: return "unidades"
        default: return ""
        }
    }
}
```

- [ ] **Step 3: Entradas en `project.pbxproj`**

Mismo procedimiento que Task H1. Asigna 2 nuevos IDs incrementales, añade las 4 secciones para cada archivo.

- [ ] **Step 4: Compilar iOS**

Run: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

- [ ] **Step 5: Commit**

```bash
git add iosApp/BarksAccountantApp/Settings/IngredientFormStoreWrapper.swift iosApp/BarksAccountantApp/Settings/IngredientFormView.swift iosApp/BarksAccountantApp.xcodeproj/project.pbxproj
git commit -m "feat(ios): add IngredientFormView with delete guard"
```

---

### Task H3: Enlace a Ingredientes desde `SettingsView` (iOS)

**Files:**
- Modify: `iosApp/BarksAccountantApp/Settings/SettingsView.swift`

- [ ] **Step 1: Ampliar `SettingsDestination`**

Localiza `enum SettingsDestination` en `SettingsView.swift` (o archivo donde esté). Añade `case ingredients`.

- [ ] **Step 2: Añadir `NavigationLink`**

Donde hay `NavigationLink(value: SettingsDestination.products)`, añade debajo:
```swift
NavigationLink(value: SettingsDestination.ingredients) {
    // mismo estilo que el de productos; label "Ingredientes"
}
```

- [ ] **Step 3: Actualizar `.navigationDestination(for: SettingsDestination.self)`**

Añade rama:
```swift
case .ingredients:
    IngredientsListView(serviceLocator: serviceLocator)
```

- [ ] **Step 4: Compilar**

Run: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

- [ ] **Step 5: Commit**

```bash
git add iosApp/BarksAccountantApp/Settings/SettingsView.swift
git commit -m "feat(ios): link Ingredientes from Settings"
```

---

### Task H4: `IngredientPickerSheet` (iOS) + sección de ingredientes en `ProductFormView`

**Files:**
- Create: `iosApp/BarksAccountantApp/Settings/IngredientPickerSheet.swift`
- Modify: `iosApp/BarksAccountantApp/Settings/ProductFormView.swift`
- Modify: `iosApp/BarksAccountantApp/Settings/ProductFormStoreWrapper.swift`

- [ ] **Step 1: Crear `IngredientPickerSheet.swift`**

```swift
import SwiftUI
import Shared

struct IngredientPickerSheet: View {
    let ingredients: [Ingredient]
    let onSelected: (Ingredient) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(ingredients, id: \.id) { i in
                Button {
                    onSelected(i)
                    dismiss()
                } label: {
                    HStack {
                        Text(i.name).fontWeight(.semibold)
                        Spacer()
                        Text(i.unit.symbol).font(.caption).foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Elegir ingrediente")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cerrar") { dismiss() }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Ampliar `ProductFormStoreWrapper`**

Añade estas propiedades `@Observable`:
```swift
private(set) var productIngredients: [ProductIngredient] = []
private(set) var ingredientQuantityTexts: [String] = []
private(set) var availableIngredients: [Ingredient] = []
private(set) var showIngredientPicker: Bool = false
```
En el callback del `FlowCollector`, añade:
```swift
self.productIngredients = (state.ingredients as? [ProductIngredient]) ?? []
self.ingredientQuantityTexts = (state.ingredientQuantityTexts as? [String]) ?? []
self.availableIngredients = (state.availableIngredients as? [Ingredient]) ?? []
self.showIngredientPicker = state.showIngredientPicker
```
Actualiza el init para recibir `ingredientRepository` y pásalo al `ProductFormStore`:
```swift
init(productRepository: ProductRepository, ingredientRepository: IngredientRepository) {
    self.store = ProductFormStore(
        productRepository: productRepository,
        ingredientRepository: ingredientRepository
    )
}
```
Añade métodos:
```swift
func openIngredientPicker() { store.dispatch(message: ProductFormMessageAddIngredientTapped.shared) }
func dismissIngredientPicker() { store.dispatch(message: ProductFormMessageDismissIngredientPicker.shared) }
func pickIngredient(_ i: Ingredient) { store.dispatch(message: ProductFormMessageIngredientPicked(ingredient: i)) }
func onIngredientQuantityChange(index: Int, text: String) {
    store.dispatch(message: ProductFormMessageIngredientQuantityChanged(index: Int32(index), text: text))
}
func removeIngredient(index: Int) {
    store.dispatch(message: ProductFormMessageIngredientRemoved(index: Int32(index)))
}
```

- [ ] **Step 3: Actualizar `ProductFormView`**

Donde se crea el wrapper:
```swift
_store = State(initialValue: ProductFormStoreWrapper(
    productRepository: serviceLocator.productRepository,
    ingredientRepository: serviceLocator.ingredientRepository
))
```
Añade Section nueva entre la de info/precio y la de Guardar:
```swift
Section("Ingredientes") {
    if store.productIngredients.isEmpty {
        Text("No hay ingredientes añadidos").foregroundStyle(.secondary)
    } else {
        ForEach(Array(store.productIngredients.enumerated()), id: \.offset) { index, ing in
            HStack {
                Text(ing.ingredientName).fontWeight(.semibold)
                Spacer()
                TextField("0", text: Binding(
                    get: { store.ingredientQuantityTexts.indices.contains(index) ? store.ingredientQuantityTexts[index] : "" },
                    set: { store.onIngredientQuantityChange(index: index, text: $0) }
                ))
                .keyboardType(.decimalPad)
                .multilineTextAlignment(.trailing)
                .frame(width: 70)
                Text(ing.unit.symbol).font(.caption).foregroundStyle(.secondary)
                Button(role: .destructive) { store.removeIngredient(index: index) } label: {
                    Image(systemName: "trash")
                }
                .buttonStyle(.borderless)
            }
        }
    }
    Button {
        store.openIngredientPicker()
    } label: {
        Label("Añadir ingrediente", systemImage: "plus")
    }
    .disabled(store.availableIngredients.isEmpty)
    if store.availableIngredients.isEmpty {
        Text("Créalos en Settings → Ingredientes").font(.caption).foregroundStyle(.secondary)
    }
}
```
Y un `.sheet`:
```swift
.sheet(isPresented: Binding(
    get: { store.showIngredientPicker },
    set: { _ in store.dismissIngredientPicker() }
)) {
    IngredientPickerSheet(
        ingredients: store.availableIngredients.filter { avail in
            !store.productIngredients.contains(where: { $0.ingredientId == avail.id })
        },
        onSelected: { ing in store.pickIngredient(ing) }
    )
}
```

- [ ] **Step 4: Entradas en `project.pbxproj`**

1 archivo nuevo (`IngredientPickerSheet.swift`). Añade las 4 secciones con IDs incrementales.

- [ ] **Step 5: Compilar iOS**

Run: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

- [ ] **Step 6: Commit**

```bash
git add iosApp/BarksAccountantApp/Settings/IngredientPickerSheet.swift iosApp/BarksAccountantApp/Settings/ProductFormView.swift iosApp/BarksAccountantApp/Settings/ProductFormStoreWrapper.swift iosApp/BarksAccountantApp.xcodeproj/project.pbxproj
git commit -m "feat(ios): add ingredient picker and recipe section to ProductForm"
```

---

### Task H5: `ProductionPlanStoreWrapper` + `ProductionPlanView`

**Files:**
- Create: `iosApp/BarksAccountantApp/Purchases/ProductionPlanStoreWrapper.swift`
- Create: `iosApp/BarksAccountantApp/Purchases/ProductionPlanView.swift`

- [ ] **Step 1: Wrapper**

```swift
import Foundation
import Shared

@Observable
final class ProductionPlanStoreWrapper {
    private(set) var availableProducts: [Product] = []
    private(set) var rows: [PlanRow] = []
    private(set) var showProductPicker: Bool = false
    private(set) var isLoading: Bool = false
    private(set) var needs: [RawMaterialNeed] = []
    private(set) var productsWithoutRecipe: [String] = []
    private(set) var error: String?

    private let store: ProductionPlanStore
    private var collector: FlowCollector<ProductionPlanState>?

    init(productRepository: ProductRepository) {
        self.store = ProductionPlanStore(productRepository: productRepository)
    }

    func start() {
        collector = FlowCollector<ProductionPlanState>(flow: store.state) { [weak self] state in
            guard let self else { return }
            self.availableProducts = (state.availableProducts as? [Product]) ?? []
            self.rows = (state.rows as? [PlanRow]) ?? []
            self.showProductPicker = state.showProductPicker
            self.isLoading = state.isLoading
            let r = state.result
            self.needs = (r.needs as? [RawMaterialNeed]) ?? []
            self.productsWithoutRecipe = (r.productsWithoutRecipe as? [String]) ?? []
            self.error = state.error
        }
        store.dispatch(message: ProductionPlanMessageStarted.shared)
    }

    func openPicker() { store.dispatch(message: ProductionPlanMessageAddProductTapped.shared) }
    func dismissPicker() { store.dispatch(message: ProductionPlanMessageDismissPicker.shared) }
    func pickProduct(_ p: Product) { store.dispatch(message: ProductionPlanMessageProductPicked(product: p)) }
    func changeQty(productId: String, text: String) {
        store.dispatch(message: ProductionPlanMessageQuantityChanged(productId: productId, text: text))
    }
    func removeRow(productId: String) {
        store.dispatch(message: ProductionPlanMessageRowRemoved(productId: productId))
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
```

- [ ] **Step 2: View**

```swift
import SwiftUI
import Shared

struct ProductionPlanView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ProductionPlanStoreWrapper

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ProductionPlanStoreWrapper(
            productRepository: serviceLocator.productRepository
        ))
    }

    var body: some View {
        Form {
            Section("Helados a producir") {
                if store.rows.isEmpty {
                    Text("Añade helados para ver la materia prima necesaria").foregroundStyle(.secondary)
                }
                ForEach(store.rows, id: \.productId) { row in
                    HStack {
                        Text(row.productName).fontWeight(.semibold)
                        Spacer()
                        TextField("0", text: Binding(
                            get: { row.quantityText },
                            set: { store.changeQty(productId: row.productId, text: $0) }
                        ))
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 60)
                        Button(role: .destructive) { store.removeRow(productId: row.productId) } label: {
                            Image(systemName: "trash")
                        }
                        .buttonStyle(.borderless)
                    }
                }
                Button {
                    store.openPicker()
                } label: {
                    Label("Añadir helado", systemImage: "plus")
                }
            }

            if !store.needs.isEmpty {
                Section("Materia prima necesaria") {
                    ForEach(store.needs, id: \.ingredientId) { need in
                        HStack {
                            Text(need.ingredientName)
                            Spacer()
                            Text("\(formatQty(need.displayQuantity)) \(need.displayUnit.symbol)").fontWeight(.semibold)
                        }
                    }
                }
            }
            if !store.productsWithoutRecipe.isEmpty {
                Section("Productos sin receta") {
                    ForEach(store.productsWithoutRecipe, id: \.self) { name in
                        Text(name).foregroundStyle(.secondary)
                    }
                    Text("No se incluyen en el cálculo").font(.caption).foregroundStyle(.secondary)
                }
            }
        }
        .navigationTitle("Planificar producción")
        .sheet(isPresented: Binding(get: { store.showProductPicker }, set: { _ in store.dismissPicker() })) {
            // Reutilizar ProductPickerSheet existente (el de Sales). Asume que expone
            // init(products: [Product], onSelected: (Product) -> Void).
            ProductPickerSheet(
                products: store.availableProducts.filter { p in
                    !store.rows.contains(where: { $0.productId == p.id })
                },
                onSelected: { p in store.pickProduct(p) }
            )
        }
        .onAppear { store.start() }
    }

    private func formatQty(_ q: Double) -> String {
        if q == q.rounded() && abs(q) < 1e12 { return String(Int(q)) }
        return String(format: "%.2f", q)
    }
}
```

> **Nota:** si la API de `ProductPickerSheet` actual no coincide exactamente con `(products:onSelected:)`, adapta la llamada al constructor real o crea un wrapper fino.

- [ ] **Step 3: Entradas en `project.pbxproj`**

2 archivos nuevos, grupo Purchases (D10005). IDs incrementales.

- [ ] **Step 4: Compilar iOS**

Run: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

- [ ] **Step 5: Commit**

```bash
git add iosApp/BarksAccountantApp/Purchases/ProductionPlanStoreWrapper.swift iosApp/BarksAccountantApp/Purchases/ProductionPlanView.swift iosApp/BarksAccountantApp.xcodeproj/project.pbxproj
git commit -m "feat(ios): add ProductionPlanView"
```

---

### Task H6: `ProductionCapacityStoreWrapper` + `ProductionCapacityView`

**Files:**
- Create: `iosApp/BarksAccountantApp/Purchases/ProductionCapacityStoreWrapper.swift`
- Create: `iosApp/BarksAccountantApp/Purchases/ProductionCapacityView.swift`

- [ ] **Step 1: Wrapper**

```swift
import Foundation
import Shared

@Observable
final class ProductionCapacityStoreWrapper {
    private(set) var availableProducts: [Product] = []
    private(set) var selectedProduct: Product?
    private(set) var selectedIngredient: ProductIngredient?
    private(set) var availableQuantityText: String = ""
    private(set) var showProductPicker: Bool = false
    private(set) var showIngredientPicker: Bool = false
    private(set) var productCount: Double?
    private(set) var others: [RawMaterialNeed] = []

    private let store: ProductionCapacityStore
    private var collector: FlowCollector<ProductionCapacityState>?

    init(productRepository: ProductRepository) {
        self.store = ProductionCapacityStore(productRepository: productRepository)
    }

    func start() {
        collector = FlowCollector<ProductionCapacityState>(flow: store.state) { [weak self] state in
            guard let self else { return }
            self.availableProducts = (state.availableProducts as? [Product]) ?? []
            self.selectedProduct = state.selectedProduct
            self.selectedIngredient = state.selectedIngredient
            self.availableQuantityText = state.availableQuantityText
            self.showProductPicker = state.showProductPicker
            self.showIngredientPicker = state.showIngredientPicker
            if let result = state.result {
                self.productCount = result.productCount
                self.others = (result.otherIngredientsNeeded as? [RawMaterialNeed]) ?? []
            } else {
                self.productCount = nil
                self.others = []
            }
        }
        store.dispatch(message: ProductionCapacityMessageStarted.shared)
    }

    func openProductPicker() { store.dispatch(message: ProductionCapacityMessageProductPickerOpened.shared) }
    func dismissProductPicker() { store.dispatch(message: ProductionCapacityMessageDismissProductPicker.shared) }
    func pickProduct(_ p: Product) { store.dispatch(message: ProductionCapacityMessageProductPicked(product: p)) }
    func openIngredientPicker() { store.dispatch(message: ProductionCapacityMessageIngredientPickerOpened.shared) }
    func dismissIngredientPicker() { store.dispatch(message: ProductionCapacityMessageDismissIngredientPicker.shared) }
    func pickIngredient(_ i: ProductIngredient) { store.dispatch(message: ProductionCapacityMessageIngredientPicked(ingredient: i)) }
    func setAvailable(_ t: String) { store.dispatch(message: ProductionCapacityMessageAvailableQuantityChanged(text: t)) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
```

- [ ] **Step 2: View**

```swift
import SwiftUI
import Shared

struct ProductionCapacityView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ProductionCapacityStoreWrapper

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ProductionCapacityStoreWrapper(
            productRepository: serviceLocator.productRepository
        ))
    }

    var body: some View {
        Form {
            Section("1. Helado") {
                Button {
                    store.openProductPicker()
                } label: {
                    HStack {
                        Text(store.selectedProduct?.name ?? "Selecciona un helado")
                            .foregroundStyle(store.selectedProduct == nil ? .secondary : .primary)
                        Spacer()
                        Image(systemName: "chevron.right").foregroundStyle(.secondary)
                    }
                }
            }
            Section("2. Ingrediente") {
                let product = store.selectedProduct
                let hasRecipe = (product?.ingredients.count ?? 0) > 0
                Button {
                    if hasRecipe { store.openIngredientPicker() }
                } label: {
                    HStack {
                        Text(
                            product == nil ? "Selecciona primero un helado"
                            : !hasRecipe ? "Este helado no tiene receta definida"
                            : store.selectedIngredient == nil ? "Elige un ingrediente"
                            : "\(store.selectedIngredient!.ingredientName) (\(store.selectedIngredient!.unit.symbol))"
                        )
                        .foregroundStyle(store.selectedIngredient == nil ? .secondary : .primary)
                        Spacer()
                        if hasRecipe {
                            Image(systemName: "chevron.right").foregroundStyle(.secondary)
                        }
                    }
                }
                .disabled(!hasRecipe)
            }
            Section("3. Tengo") {
                HStack {
                    TextField("Cantidad", text: Binding(
                        get: { store.availableQuantityText },
                        set: { store.setAvailable($0) }
                    ))
                    .keyboardType(.decimalPad)
                    .disabled(store.selectedIngredient == nil)
                    Text(store.selectedIngredient?.unit.symbol ?? "").foregroundStyle(.secondary)
                }
            }
            if let count = store.productCount {
                Section("Resultado") {
                    if count < 1.0 {
                        Text("No alcanza ni para un helado").fontWeight(.semibold)
                    } else {
                        Text("Puedes hacer \(formatCapacity(count)) helados de \(store.selectedProduct?.name ?? "")").fontWeight(.semibold)
                        if !store.others.isEmpty {
                            Text("Para esa cantidad necesitarás además:").font(.caption).foregroundStyle(.secondary)
                            ForEach(store.others, id: \.ingredientId) { need in
                                HStack {
                                    Text(need.ingredientName)
                                    Spacer()
                                    Text("\(formatCapacity(need.displayQuantity)) \(need.displayUnit.symbol)")
                                }
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("¿Qué puedo producir?")
        .sheet(isPresented: Binding(get: { store.showProductPicker }, set: { _ in store.dismissProductPicker() })) {
            ProductPickerSheet(
                products: store.availableProducts,
                onSelected: { p in store.pickProduct(p) }
            )
        }
        .sheet(isPresented: Binding(get: { store.showIngredientPicker }, set: { _ in store.dismissIngredientPicker() })) {
            NavigationStack {
                List((store.selectedProduct?.ingredients as? [ProductIngredient]) ?? [], id: \.ingredientId) { i in
                    Button {
                        store.pickIngredient(i)
                        store.dismissIngredientPicker()
                    } label: {
                        HStack {
                            Text(i.ingredientName)
                            Spacer()
                            Text(i.unit.symbol).font(.caption).foregroundStyle(.secondary)
                        }
                    }
                }
                .navigationTitle("Elegir ingrediente")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cerrar") { store.dismissIngredientPicker() }
                    }
                }
            }
        }
        .onAppear { store.start() }
    }

    private func formatCapacity(_ q: Double) -> String {
        if q == q.rounded() && abs(q) < 1e12 { return String(Int(q)) }
        return String(format: "%.1f", q)
    }
}
```

- [ ] **Step 3: Entradas en `project.pbxproj`**

2 archivos nuevos, grupo Purchases. IDs incrementales.

- [ ] **Step 4: Compilar iOS**

Run: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

- [ ] **Step 5: Commit**

```bash
git add iosApp/BarksAccountantApp/Purchases/ProductionCapacityStoreWrapper.swift iosApp/BarksAccountantApp/Purchases/ProductionCapacityView.swift iosApp/BarksAccountantApp.xcodeproj/project.pbxproj
git commit -m "feat(ios): add ProductionCapacityView"
```

---

### Task H7: Accesos desde `PurchasesListView`

**Files:**
- Modify: `iosApp/BarksAccountantApp/Purchases/PurchasesListView.swift`

- [ ] **Step 1: Añadir dos `NavigationLink` arriba**

Al principio del contenedor de `PurchasesListView`, antes del `List` de compras, añade una `Section` con dos `NavigationLink`:

```swift
Section {
    NavigationLink("Planificar producción") {
        ProductionPlanView(serviceLocator: serviceLocator)
    }
    NavigationLink("¿Qué puedo producir?") {
        ProductionCapacityView(serviceLocator: serviceLocator)
    }
}
```

> Si `PurchasesListView` usa `List { ... }` como root, añade la `Section` dentro del mismo `List`. Si es otra estructura, adapta manteniendo los enlaces arriba.

- [ ] **Step 2: Compilar**

Run: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

- [ ] **Step 3: Verificar manualmente en simulador**

Abre la app → Compras → tap "Planificar producción" → añade un helado con receta existente → comprueba que aparece la materia prima. Vuelve, tap "¿Qué puedo producir?" → elige helado, ingrediente, escribe cantidad → comprueba resultado decimal.

- [ ] **Step 4: Commit**

```bash
git add iosApp/BarksAccountantApp/Purchases/PurchasesListView.swift
git commit -m "feat(ios): link plan and capacity screens from Compras"
```

---

## Fase I — Verificación final y smoke test

### Task I1: Ejecutar todos los tests de shared

- [ ] **Step 1: Correr suite completa**

Run: `./gradlew :shared:iosSimulatorArm64Test`
Expected: BUILD SUCCESSFUL, 8 tests passed.

- [ ] **Step 2: Android assemble**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: iOS build**

Run: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
Expected: BUILD SUCCEEDED

---

### Task I2: Smoke test manual (ambas plataformas)

- [ ] **Step 1: Crear al menos 3 ingredientes** (Pollo/g, Leche/ml, Huevo/u) desde Settings.
- [ ] **Step 2: Editar un producto existente** y añadirle una receta (p.ej. 150 g pollo + 100 ml leche + 1 huevo).
- [ ] **Step 3: Crear un producto nuevo sin ingredientes** y verificar que se guarda y que aparece en "Productos sin receta" en planificar producción.
- [ ] **Step 4: Planificar producción**: añadir 3 del producto con receta + 2 del producto sin receta. Verificar totales esperados (450 g pollo, 300 ml leche, 3 huevos — display en g si < 1000, en kg si ≥ 1000).
- [ ] **Step 5: Cálculo inverso**: seleccionar el producto con receta, ingrediente Pollo, cantidad 100. Verificar que el count es `100 / 150 ≈ 0.7` → mostrar "No alcanza ni para un helado". Probar con 450 → debe salir 3 helados, 300 ml leche, 3 huevos.
- [ ] **Step 6: Intentar borrar un ingrediente en uso**: ir a Settings → Ingredientes → Pollo → Eliminar. Verificar que aparece dialog "No se puede eliminar: se usa en [producto]".
- [ ] **Step 7: Quitar el ingrediente de los productos y reintentar borrar**: ahora debe borrarse.

---

## Notas finales

- **`project.pbxproj`**: antes de añadir cada archivo Swift nuevo, haz `grep -c "isa = PBXBuildFile" iosApp/BarksAccountantApp.xcodeproj/project.pbxproj` para asegurarte de que la numeración de tus IDs nuevos no choca con los existentes. Si los `A100XX`/`B100XX` ya están agotados, pasa a `A101XX`/`B101XX`.
- **Spec**: cualquier divergencia entre plan y código implementado debe reflejarse como follow-up issue, no silenciarse.
- **Dispose**: todos los `StoreWrapper` cierran el `collector` y disponen el store en `deinit`. Los `@Composable` usan `DisposableEffect` → `onDispose { store.dispose() }`. No omitas esto.
