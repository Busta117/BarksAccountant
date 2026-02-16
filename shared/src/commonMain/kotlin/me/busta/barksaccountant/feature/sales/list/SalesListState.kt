package me.busta.barksaccountant.feature.sales.list

import me.busta.barksaccountant.model.Sale

data class SalesListState(
    val sales: List<Sale> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
