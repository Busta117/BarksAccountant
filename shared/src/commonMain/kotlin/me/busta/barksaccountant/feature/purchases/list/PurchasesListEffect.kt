package me.busta.barksaccountant.feature.purchases.list

sealed interface PurchasesListEffect {
    data object LoadPurchases : PurchasesListEffect
}
