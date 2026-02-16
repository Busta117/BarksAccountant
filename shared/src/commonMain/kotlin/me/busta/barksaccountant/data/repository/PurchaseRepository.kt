package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Purchase

interface PurchaseRepository {
    suspend fun getPurchases(): List<Purchase>
    suspend fun getPurchase(id: String): Purchase?
    suspend fun savePurchase(purchase: Purchase): Purchase
    suspend fun updatePurchase(purchase: Purchase): Purchase
    suspend fun deletePurchase(id: String)
}
