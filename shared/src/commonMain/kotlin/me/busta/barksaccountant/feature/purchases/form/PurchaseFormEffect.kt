package me.busta.barksaccountant.feature.purchases.form

import me.busta.barksaccountant.model.Purchase

sealed interface PurchaseFormEffect {
    data class LoadPurchase(val purchaseId: String?) : PurchaseFormEffect
    data class SavePurchase(val purchase: Purchase) : PurchaseFormEffect
    data class UpdatePurchase(val purchase: Purchase) : PurchaseFormEffect
    data class DeletePurchase(val id: String) : PurchaseFormEffect
}
