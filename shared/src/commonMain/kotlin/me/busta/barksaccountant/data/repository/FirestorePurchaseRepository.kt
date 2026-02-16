package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.data.FirestoreService
import me.busta.barksaccountant.model.Purchase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FirestorePurchaseRepository(
    private val firestoreService: FirestoreService,
    private val appId: String
) : PurchaseRepository {

    private val collectionPath get() = "apps/$appId/purchases"

    override suspend fun getPurchases(): List<Purchase> {
        return firestoreService.getDocuments(collectionPath).map { mapToPurchase(it) }
    }

    override suspend fun getPurchase(id: String): Purchase? {
        val data = firestoreService.getDocument(collectionPath, id) ?: return null
        return mapToPurchase(data, id)
    }

    override suspend fun savePurchase(purchase: Purchase): Purchase {
        val newId = Uuid.random().toString()
        val newPurchase = purchase.copy(id = newId)
        firestoreService.setDocument(collectionPath, newId, purchaseToMap(newPurchase))
        return newPurchase
    }

    override suspend fun updatePurchase(purchase: Purchase): Purchase {
        firestoreService.setDocument(collectionPath, purchase.id, purchaseToMap(purchase))
        return purchase
    }

    override suspend fun deletePurchase(id: String) {
        firestoreService.deleteDocument(collectionPath, id)
    }

    private fun purchaseToMap(purchase: Purchase): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "title" to purchase.title,
            "value" to purchase.value,
            "date" to purchase.date,
            "createdBy" to purchase.createdBy
        )
        purchase.description?.let { map["description"] = it }
        return map
    }

    private fun mapToPurchase(data: Map<String, Any>, overrideId: String? = null): Purchase {
        return Purchase(
            id = overrideId ?: (data["id"] as? String ?: ""),
            title = data["title"] as? String ?: "",
            description = data["description"] as? String,
            value = (data["value"] as? Number)?.toDouble() ?: 0.0,
            date = data["date"] as? String ?: "",
            createdBy = data["createdBy"] as? String ?: ""
        )
    }
}
