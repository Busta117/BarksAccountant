package me.busta.barksaccountant.feature.purchases.list

import me.busta.barksaccountant.model.Purchase

data class PurchasesListState(
    val purchases: List<Purchase> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
