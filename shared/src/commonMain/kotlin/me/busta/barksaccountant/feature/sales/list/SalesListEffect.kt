package me.busta.barksaccountant.feature.sales.list

sealed interface SalesListEffect {
    data object LoadSales : SalesListEffect
}
