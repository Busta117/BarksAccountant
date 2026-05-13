package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Ingredient

interface IngredientRepository {
    suspend fun getIngredients(): List<Ingredient>
    suspend fun getIngredient(id: String): Ingredient?
    suspend fun saveIngredient(ingredient: Ingredient): Ingredient
    suspend fun updateIngredient(ingredient: Ingredient): Ingredient
    suspend fun deleteIngredient(id: String)
}
