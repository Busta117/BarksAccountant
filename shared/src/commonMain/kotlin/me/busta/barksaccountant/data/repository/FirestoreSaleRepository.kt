package me.busta.barksaccountant.data.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.busta.barksaccountant.data.FirestoreService
import me.busta.barksaccountant.model.Sale
import me.busta.barksaccountant.model.SaleProduct

class FirestoreSaleRepository(
    private val firestoreService: FirestoreService,
    private val appId: String
) : SaleRepository {

    private val collectionPath get() = "apps/$appId/sales"

    override suspend fun getSales(): List<Sale> {
        return firestoreService.getDocuments(collectionPath).map { mapToSale(it) }
    }

    override suspend fun getSale(id: String): Sale? {
        val data = firestoreService.getDocument(collectionPath, id) ?: return null
        return mapToSale(data, id)
    }

    override suspend fun saveSale(sale: Sale): Sale {
        val newId = generateNextId()
        val newSale = sale.copy(id = newId)
        firestoreService.setDocument(collectionPath, newId, saleToMap(newSale))
        return newSale
    }

    override suspend fun updateSale(sale: Sale): Sale {
        firestoreService.setDocument(collectionPath, sale.id, saleToMap(sale))
        return sale
    }

    override suspend fun deleteSale(id: String) {
        firestoreService.deleteDocument(collectionPath, id)
    }

    private suspend fun generateNextId(): String {
        val year = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
        val yearPrefix = "$year-"
        val existingIds = getSales().map { it.id }
        val maxNumber = existingIds
            .filter { it.startsWith(yearPrefix) }
            .mapNotNull { it.removePrefix(yearPrefix).toIntOrNull() }
            .maxOrNull() ?: 0
        return "$yearPrefix${(maxNumber + 1).toString().padStart(4, '0')}"
    }

    private fun saleToMap(sale: Sale): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "clientName" to sale.clientName,
            "orderDate" to sale.orderDate,
            "totalPrice" to sale.totalPrice,
            "isPaid" to sale.isPaid,
            "isDelivered" to sale.isDelivered,
            "createdBy" to sale.createdBy
        )
        sale.deliveryDate?.let { map["deliveryDate"] = it }
        map["products"] = sale.products.map { product ->
            mapOf<String, Any>(
                "productId" to product.productId,
                "name" to product.name,
                "unitPrice" to product.unitPrice,
                "quantity" to product.quantity
            )
        }
        return map
    }

    private fun mapToSale(data: Map<String, Any>, overrideId: String? = null): Sale {
        val id = overrideId ?: (data["id"] as? String ?: "")
        val productsData = (data["products"] as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
        return Sale(
            id = id,
            clientName = data["clientName"] as? String ?: "",
            orderDate = data["orderDate"] as? String ?: "",
            deliveryDate = data["deliveryDate"] as? String,
            products = productsData.map { productMap ->
                SaleProduct(
                    productId = productMap["productId"] as? String ?: "",
                    name = productMap["name"] as? String ?: "",
                    unitPrice = (productMap["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                    quantity = (productMap["quantity"] as? Number)?.toInt() ?: 0
                )
            },
            totalPrice = (data["totalPrice"] as? Number)?.toDouble() ?: 0.0,
            isPaid = data["isPaid"] as? Boolean ?: false,
            isDelivered = data["isDelivered"] as? Boolean ?: false,
            createdBy = data["createdBy"] as? String ?: ""
        )
    }
}
