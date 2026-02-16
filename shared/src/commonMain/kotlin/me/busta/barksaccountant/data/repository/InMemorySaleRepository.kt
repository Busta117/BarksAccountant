package me.busta.barksaccountant.data.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.busta.barksaccountant.model.Sale
import me.busta.barksaccountant.model.SaleProduct

class InMemorySaleRepository : SaleRepository {
    private val sales = mutableListOf(
        Sale(
            id = "2025-0001",
            clientName = "Juan García",
            orderDate = "2025-02-10",
            deliveryDate = null,
            products = listOf(
                SaleProduct(productId = "p1", name = "Camiseta", unitPrice = 15.0, quantity = 3),
                SaleProduct(productId = "p2", name = "Pantalón", unitPrice = 25.0, quantity = 2)
            ),
            totalPrice = 95.0,
            isPaid = false,
            isDelivered = false,
            createdBy = "Santiago"
        ),
        Sale(
            id = "2025-0002",
            clientName = "Ana López",
            orderDate = "2025-02-08",
            deliveryDate = "2025-02-12",
            products = listOf(
                SaleProduct(productId = "p3", name = "Zapatos", unitPrice = 45.0, quantity = 1)
            ),
            totalPrice = 45.0,
            isPaid = true,
            isDelivered = true,
            createdBy = "Santiago"
        ),
        Sale(
            id = "2025-0003",
            clientName = "Pedro Martínez",
            orderDate = "2025-02-13",
            deliveryDate = null,
            products = listOf(
                SaleProduct(productId = "p4", name = "Gorra", unitPrice = 10.0, quantity = 5),
                SaleProduct(productId = "p5", name = "Chaqueta", unitPrice = 60.0, quantity = 1)
            ),
            totalPrice = 110.0,
            isPaid = false,
            isDelivered = false,
            createdBy = "Carlos"
        )
    )

    override suspend fun getSales(): List<Sale> {
        return sales.toList()
    }

    override suspend fun getSale(id: String): Sale? {
        return sales.find { it.id == id }
    }

    override suspend fun saveSale(sale: Sale): Sale {
        val year = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
        val yearPrefix = "$year-"
        val maxNumber = sales
            .map { it.id }
            .filter { it.startsWith(yearPrefix) }
            .mapNotNull { it.removePrefix(yearPrefix).toIntOrNull() }
            .maxOrNull() ?: 0
        val newId = "$yearPrefix${(maxNumber + 1).toString().padStart(4, '0')}"
        val newSale = sale.copy(id = newId)
        sales.add(newSale)
        return newSale
    }

    override suspend fun updateSale(sale: Sale): Sale {
        val index = sales.indexOfFirst { it.id == sale.id }
        if (index >= 0) {
            sales[index] = sale
        }
        return sale
    }

    override suspend fun deleteSale(id: String) {
        sales.removeAll { it.id == id }
    }
}
