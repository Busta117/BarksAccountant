package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: String): Product?
    suspend fun saveProduct(product: Product): Product
    suspend fun updateProduct(product: Product): Product
    suspend fun deleteProduct(id: String)
}
