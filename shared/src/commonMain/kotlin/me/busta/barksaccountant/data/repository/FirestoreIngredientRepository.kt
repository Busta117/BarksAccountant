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
