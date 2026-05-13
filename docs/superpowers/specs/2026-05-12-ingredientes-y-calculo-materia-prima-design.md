# Ingredientes y cálculo de materia prima

Diseño de la ampliación del modelo de productos (helados) en BarksAccountant para soportar recetas con ingredientes, y dos herramientas de cálculo de materia prima.

## Objetivo

Ampliar BarksAccountant con:

1. Un catálogo global de ingredientes (cada uno con nombre + unidad fija).
2. La posibilidad de que cada producto (helado) tenga una receta: lista de ingredientes con cantidad.
3. Dos herramientas de planificación dentro de la pestaña Compras:
   - **Cálculo directo**: "quiero hacer X helados de cada tipo" → cantidades totales de materia prima a comprar.
   - **Cálculo inverso**: "tengo X de un ingrediente" → cuántos helados puedo hacer de un sabor concreto y qué otros ingredientes me hacen falta.

## Decisiones clave

- **Catálogo separado** de ingredientes (colección Firestore propia), no inline por producto. Garantiza que "Pollo" en un helado y en otro refiere al mismo ingrediente.
- **Unidad fija por ingrediente** (al crear el ingrediente en el catálogo, se fija). Unidades: `g`, `kg`, `ml`, `l`, `u`.
- **Conversión automática** `g↔kg` y `ml↔l` solo para visualización de totales (si la base es `g` y el total es ≥ 1000, muestra en `kg`). No hay conversión en los inputs de cantidad, solo en los display de resultados.
- **Cálculos no se persisten**. Son herramientas de consulta, no datos históricos.
- **Borrado bloqueado**: un ingrediente referenciado por algún producto no se puede borrar; el error lista los productos afectados.
- **Cantidades decimales** en las recetas (0.5 l, 2.5 huevos).
- **Ingredientes opcionales en el producto**. Los productos existentes cargan con lista vacía. `canSave` no requiere ingredientes.
- **Cálculo inverso**: el "tengo X" se expresa en la unidad base del ingrediente (sin conversión en el input). El número de helados resultante se muestra con decimales (ej. 3.7).

## Modelo de datos (shared/commonMain)

### Nuevo: `IngredientUnit`

```kotlin
enum class IngredientUnit(val symbol: String) {
    GRAMS("g"),
    KILOGRAMS("kg"),
    MILLILITERS("ml"),
    LITERS("l"),
    UNITS("u")
}
```

### Nuevo: `Ingredient`

```kotlin
data class Ingredient(
    val id: String,
    val name: String,
    val unit: IngredientUnit
)
```

- `id`: `kotlin.uuid.Uuid.random().toString()` (patrón existente, con `@OptIn(ExperimentalUuidApi::class)`).
- `unit` es inmutable una vez guardado: el form en modo edición muestra la unidad como read-only.

### Nuevo: `ProductIngredient`

```kotlin
data class ProductIngredient(
    val ingredientId: String,
    val ingredientName: String,
    val unit: IngredientUnit,
    val quantity: Double
)
```

`ingredientName` y `unit` se denormalizan para pintar la receta sin leer todo el catálogo (mismo patrón que `SaleProduct`).

### Modificado: `Product`

```kotlin
data class Product(
    val id: String,
    val name: String,
    val unitPrice: Double,
    val ingredients: List<ProductIngredient> = emptyList()
)
```

El valor por defecto hace que los productos existentes en Firestore se deserialicen sin romper.

## Persistencia (Firestore)

- Nueva colección: `apps/{appId}/ingredients/{ingredientId}` con campos `id`, `name`, `unit` (guardada como `String` = `unit.name`).
- `apps/{appId}/products/{productId}` gana campo `ingredients: Array<Map>`, cada map con `ingredientId`, `ingredientName`, `unit`, `quantity`.

## Repositorios

### Nuevo: `IngredientRepository` (shared/commonMain)

```kotlin
interface IngredientRepository {
    suspend fun getIngredients(): List<Ingredient>
    suspend fun getIngredient(id: String): Ingredient?
    suspend fun saveIngredient(ingredient: Ingredient)
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(id: String)
}
```

- `FirestoreIngredientRepository`: análogo a `FirestoreProductRepository`, colección `apps/{appId}/ingredients`.
- `InMemoryIngredientRepository`: 3-5 ejemplos (Pollo/g, Ternera/g, Huevo/u, Leche/ml, Guisantes/g) para desarrollo.

### Registro en `ServiceLocator`

Añadir `val ingredientRepository: IngredientRepository` con el mismo patrón que los existentes (re-crea para tomar el `appId` actual).

### Borrado con guardia

La guardia se implementa en el **Effect** del `IngredientFormStore`, no en el repositorio (el repo queda tonto, coherente con el resto):

1. `DeleteIngredient` effect → carga `productRepository.getProducts()`.
2. Filtra productos que contienen el `ingredientId` en su lista.
3. Si hay productos → despacha `DeleteBlocked(productNames: List<String>)` → estado muestra error con nombres concatenados.
4. Si no → llama `ingredientRepository.deleteIngredient(id)` y despacha `IngredientDeleted`.

## Feature: Settings → Ingredientes

Nueva entrada en Settings al mismo nivel que Productos y Clientes.

### Stores (shared/commonMain/feature/settings/ingredients/)

```
list/
  IngredientsListState.kt  (ingredients, isLoading, error)
  IngredientsListMessage.kt (Started, IngredientsLoaded, LoadFailed)
  IngredientsListEffect.kt (LoadIngredients)
  IngredientsListStore.kt
form/
  IngredientFormState.kt (ingredientId, isEditing, name, unit, isSaving,
                           savedSuccessfully, showDeleteConfirm, deleteBlockedBy,
                           deletedSuccessfully, error)
  IngredientFormMessage.kt (Started, LoadIngredient, IngredientLoaded, NameChanged,
                             UnitChanged, Save, Saved, SaveFailed,
                             Delete, DeleteConfirmed, DeleteBlocked, Deleted, DeleteFailed,
                             ShowDeleteConfirm, DismissDeleteConfirm)
  IngredientFormEffect.kt (LoadIngredient(id), SaveIngredient, UpdateIngredient,
                            DeleteIngredient(id) — chequea productos primero)
  IngredientFormStore.kt
```

`IngredientFormState.canSave`: `name.isNotBlank() && !isSaving`.

### UI Android (Jetpack Compose)

- `androidApp/.../ui/screen/settings/IngredientsListScreen.kt`: `LazyColumn` de ingredientes. Cada item: nombre + chip a la derecha con `unit.symbol`. Empty state "No hay ingredientes". Botón flotante + para crear.
- `androidApp/.../ui/screen/settings/IngredientFormScreen.kt`: `OutlinedTextField` nombre, `ExposedDropdownMenuBox` para unidad (deshabilitado en edición), botón guardar, botón borrar con `AlertDialog` de confirmación. Si borrado bloqueado, mostrar `AlertDialog` distinto con la lista de productos que lo usan.
- Rutas nuevas en `MainScreen.kt` NavHost: `ingredients_list`, `ingredient_form?ingredientId={id}`.
- `SettingsScreen.kt`: añadir item "Ingredientes" entre Productos y Clientes (o al lado).

### UI iOS (SwiftUI)

- `iosApp/BarksAccountantApp/Settings/IngredientsListView.swift` + `IngredientsListStoreWrapper.swift`.
- `iosApp/BarksAccountantApp/Settings/IngredientFormView.swift` + `IngredientFormStoreWrapper.swift`.
- `SettingsDestination` enum añade `.ingredients`; push a `IngredientsListView`.
- Nuevo enum `IngredientDestination.form(id: String?)` para navegar del listado al form.
- `SettingsView.swift`: añadir `NavigationLink(value: SettingsDestination.ingredients)`.

### `project.pbxproj`

4 archivos Swift nuevos → entradas en las 4 secciones habituales (PBXBuildFile, PBXFileReference, PBXGroup children de Settings D10006, PBXSourcesBuildPhase). IDs incrementales tipo `A100XX` / `B100XX` a partir del último usado.

## Feature: Producto con receta

### Cambios en `ProductFormState`

Añadir:

```kotlin
val ingredients: List<ProductIngredient> = emptyList(),
val ingredientQuantityTexts: List<String> = emptyList(), // paralela a ingredients, para binding del input
val availableIngredients: List<Ingredient> = emptyList(),
val showIngredientPicker: Boolean = false,
```

Mantener precio como `String` igual que hoy. Las cantidades también se editan como `String` y se convierten a `Double` al guardar (igual patrón que precio).

`canSave` sigue siendo `name.isNotBlank() && price.toDoubleOrNull() != null && price > 0`. Adicionalmente: si `ingredients` no está vacío, todas las cantidades deben ser > 0 y parseables.

### Cambios en `ProductFormMessage` / `Effect`

Mensajes nuevos:

- `IngredientsLoaded(List<Ingredient>)`
- `AddIngredientClicked` → estado `showIngredientPicker = true`
- `DismissIngredientPicker`
- `IngredientPicked(Ingredient)` → añade `ProductIngredient(id, name, unit, quantity = 0.0)` si no está ya
- `IngredientQuantityChanged(index: Int, text: String)`
- `IngredientRemoved(index: Int)`

Effect nuevo:

- `LoadIngredients` (se dispara en `Started`, en paralelo con `LoadProduct` cuando edita)

### Picker reutilizable

- Android: `IngredientPickerDialog` — `AlertDialog` con `LazyColumn`, cada item nombre + chip unidad.
- iOS: `IngredientPickerSheet` — `.sheet` con `List`.

Se usa tanto desde el form de producto como desde la capacity screen (que filtra solo los del producto elegido — no usa el catálogo global sino la receta del producto).

### UI del form (Android + iOS)

Orden vertical:
1. Nombre.
2. Precio.
3. Sección "Ingredientes":
   - Cabecera "Ingredientes" + botón `+`.
   - Si vacío: texto sutil "No hay ingredientes aún".
   - Filas: nombre · input numérico cantidad · `unit.symbol` · botón papelera.
   - Si el catálogo global está vacío, mostrar nota: "No hay ingredientes. Créalos en Settings → Ingredientes." (el form sigue pudiéndose guardar sin ingredientes).
4. Botón Guardar.
5. Botón Borrar (solo en edición, con confirmación existente).

## Feature: Cálculos en Compras

### Navegación

La pestaña Compras hoy muestra `PurchasesListScreen` con el listado. Se añaden **dos entradas arriba del listado** (dos cards / dos filas con icono y flecha), antes del `LazyColumn` de compras:

- "Planificar producción" → `ProductionPlanScreen` / `ProductionPlanView`
- "¿Qué puedo producir?" → `ProductionCapacityScreen` / `ProductionCapacityView`

El listado de compras existentes queda debajo sin tocar. El botón `+` para crear purchase se mantiene en la TopAppBar o donde esté hoy.

Rutas Android nuevas: `production_plan`, `production_capacity`.
iOS: dos `NavigationLink` en `PurchasesListView` hacia los nuevos views.

### Calculador puro (shared/commonMain/feature/purchases/calculation/)

```kotlin
// RawMaterialCalculator.kt

data class RawMaterialNeed(
    val ingredientId: String,
    val ingredientName: String,
    val baseUnit: IngredientUnit,       // unidad almacenada (recipe base)
    val totalQuantity: Double,          // en baseUnit
    val displayUnit: IngredientUnit,    // unidad ajustada para mostrar
    val displayQuantity: Double
)

data class PlanItem(
    val productId: String,
    val productName: String,
    val quantity: Int
)

data class PlanResult(
    val needs: List<RawMaterialNeed>,
    val productsWithoutRecipe: List<String>  // nombres de productos en el plan sin ingredientes
)

data class CapacityResult(
    val productCount: Double,                         // decimal, p.ej. 3.7
    val otherIngredientsNeeded: List<RawMaterialNeed>
)

object RawMaterialCalculator {
    fun computePlan(plan: List<PlanItem>, products: List<Product>): PlanResult
    fun computeCapacity(
        product: Product,
        limitingIngredient: ProductIngredient,
        available: Double
    ): CapacityResult

    // internal helpers
    private fun toDisplay(unit: IngredientUnit, qty: Double): Pair<IngredientUnit, Double>
}
```

Reglas de `toDisplay`:
- `GRAMS` y `qty >= 1000` → `KILOGRAMS`, `qty / 1000`
- `MILLILITERS` y `qty >= 1000` → `LITERS`, `qty / 1000`
- Resto: sin cambio.
- `KILOGRAMS` y `LITERS` se muestran tal cual (no hay división a `g` / `ml`).

Redondeo de display: 2 decimales para `kg`/`l`, 0 decimales para enteros exactos, 1-2 decimales para decimales no enteros. Helper `formatQuantity(qty: Double): String` que quita `.0` innecesarios.

### Tests unitarios (commonTest)

`RawMaterialCalculatorTest.kt`:
- suma de un mismo ingrediente en varios helados
- auto-display `g` → `kg` al pasar 1000
- auto-display `ml` → `l` al pasar 1000
- `UNITS` no convierte nunca
- producto sin receta queda en `productsWithoutRecipe`
- `computeCapacity` devuelve 3.7 con un available intermedio
- `computeCapacity` con `available < quantity` devuelve count < 1
- `computeCapacity` excluye el ingrediente limitante de `otherIngredientsNeeded`

### Store: cálculo directo

`feature/purchases/productionplan/`

```
ProductionPlanState(
  availableProducts: List<Product>,
  items: List<PlanItem>,
  showProductPicker: Boolean,
  isLoading: Boolean,
  error: String?
)
```

`state.result: PlanResult?` es un computed getter que llama a `RawMaterialCalculator.computePlan(items, availableProducts)`.

Mensajes: `Started`, `ProductsLoaded`, `LoadFailed`, `AddProductClicked`, `DismissPicker`, `ProductPicked(Product)`, `QuantityChanged(productId, text)`, `ItemRemoved(productId)`.

Effect: `LoadProducts`.

Reglas:
- Un producto solo puede estar una vez en `items`. Si se vuelve a elegir, se ignora.
- `quantity` inicial = 1. Si el input es vacío o 0, se muestra pero no se incluye en el cálculo (o sí con 0, trivial).
- Productos con `ingredients.isEmpty()` se incluyen en `items` pero aparecen en `productsWithoutRecipe`, no en `needs`.

### Store: cálculo inverso

`feature/purchases/productioncapacity/`

```
ProductionCapacityState(
  availableProducts: List<Product>,
  selectedProduct: Product?,
  selectedIngredient: ProductIngredient?,
  availableQuantityText: String,
  showProductPicker: Boolean,
  showIngredientPicker: Boolean,
  isLoading: Boolean,
  error: String?
)
```

`state.result: CapacityResult?` es un computed getter: solo válido si `selectedProduct != null && selectedIngredient != null && availableQuantityText.toDoubleOrNull() != null && > 0`.

Mensajes: `Started`, `ProductsLoaded`, `LoadFailed`, `ProductPickerOpened`, `ProductPicked(Product)` (resetea `selectedIngredient` y `availableQuantityText`), `IngredientPickerOpened`, `IngredientPicked(ProductIngredient)`, `AvailableQuantityChanged(text)`, dismiss pickers.

### UI: `ProductionPlanScreen` / `ProductionPlanView`

1. Título "Planificar producción".
2. Lista de items: nombre helado · input cantidad · papelera.
3. Botón "+ Añadir helado" → `ProductPicker` existente (pero sin mostrar precio — se reutiliza o se añade una variante).
4. Si `items` vacío: empty state "Añade helados para ver la materia prima necesaria".
5. Si `items` no vacío: sección "Materia prima necesaria" con lista (nombre — cantidad + unidad display).
6. Si `productsWithoutRecipe` no vacío: sección "Productos sin receta" en gris con los nombres y texto "No se incluyen en el cálculo".

### UI: `ProductionCapacityScreen` / `ProductionCapacityView`

1. Título "¿Qué puedo producir?".
2. Campo 1 — selector de helado (tap → picker). Placeholder "Selecciona un helado".
3. Campo 2 — selector de ingrediente (disabled hasta elegir helado; si el producto no tiene receta, disabled con nota "Este helado no tiene receta definida"). Tap → `IngredientPickerSheet/Dialog` filtrado por `selectedProduct.ingredients`.
4. Campo 3 — input numérico "Tengo" con label de la unidad base del ingrediente (disabled hasta elegir ingrediente).
5. Card resultado (solo con los 3 campos válidos):
   - "Puedes hacer **{productCount}** helados de **{productName}**" (formateo: si entero, sin decimales; si decimal, 1 decimal).
   - Si `productCount < 1`: "No alcanza ni para un helado".
   - Si `productCount > 0`: subtítulo "Para esa cantidad necesitarás además:" + lista de `otherIngredientsNeeded`.

## Estado de implementación esperado

Terminado el trabajo:

1. Catálogo de ingredientes creable desde Settings → Ingredientes (Android + iOS) con borrado guardado.
2. El form de producto permite añadir/quitar ingredientes de su receta y guardar.
3. Productos existentes siguen funcionando (lista vacía de ingredientes).
4. Nueva pestaña Compras muestra 2 accesos arriba: planificar producción y capacidad.
5. Planificación directa calcula y muestra materia prima con auto-display g↔kg, ml↔l.
6. Cálculo inverso calcula cuántos helados se pueden hacer (decimal) y el resto de ingredientes necesarios.
7. Tests unitarios de `RawMaterialCalculator` pasan.
8. `project.pbxproj` actualizado con los nuevos archivos Swift.
