package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.data.FirestoreService
import me.busta.barksaccountant.model.IngredientUnit
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FirestoreProductRepository(
    private val firestoreService: FirestoreService,
    private val appId: String
) : ProductRepository {

    private val collectionPath get() = "apps/$appId/products"

    override suspend fun getProducts(): List<Product> {
        return firestoreService.getDocuments(collectionPath).map { mapToProduct(it) }
    }

    override suspend fun getProduct(id: String): Product? {
        val data = firestoreService.getDocument(collectionPath, id) ?: return null
        return mapToProduct(data, id)
    }

    override suspend fun saveProduct(product: Product): Product {
        val newId = Uuid.random().toString()
        val newProduct = product.copy(id = newId)
        firestoreService.setDocument(collectionPath, newId, productToMap(newProduct))
        return newProduct
    }

    override suspend fun updateProduct(product: Product): Product {
        firestoreService.setDocument(collectionPath, product.id, productToMap(product))
        return product
    }

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

    override suspend fun deleteProduct(id: String) {
        firestoreService.deleteDocument(collectionPath, id)
    }

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
}
