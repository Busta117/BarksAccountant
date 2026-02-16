package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Product
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class InMemoryProductRepository : ProductRepository {
    private val products = mutableListOf(
        Product(id = "p1", name = "Camiseta", unitPrice = 15.0),
        Product(id = "p2", name = "Pantalón", unitPrice = 25.0),
        Product(id = "p3", name = "Zapatos", unitPrice = 45.0),
        Product(id = "p4", name = "Gorra", unitPrice = 10.0),
        Product(id = "p5", name = "Chaqueta", unitPrice = 60.0)
    )

    override suspend fun getProducts(): List<Product> {
        return products.toList()
    }

    override suspend fun getProduct(id: String): Product? {
        return products.find { it.id == id }
    }

    override suspend fun saveProduct(product: Product): Product {
        val newProduct = product.copy(id = Uuid.random().toString())
        products.add(newProduct)
        return newProduct
    }

    override suspend fun updateProduct(product: Product): Product {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            products[index] = product
        }
        return product
    }
}
