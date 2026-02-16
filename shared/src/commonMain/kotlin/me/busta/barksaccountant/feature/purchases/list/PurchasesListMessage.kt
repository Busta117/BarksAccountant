package me.busta.barksaccountant.feature.purchases.list

import me.busta.barksaccountant.model.Purchase

sealed interface PurchasesListMessage {
    data object Started : PurchasesListMessage
    data class PurchasesLoaded(val purchases: List<Purchase>) : PurchasesListMessage
    data class ErrorOccurred(val error: String) : PurchasesListMessage
}
