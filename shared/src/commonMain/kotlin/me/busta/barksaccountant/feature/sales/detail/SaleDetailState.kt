package me.busta.barksaccountant.feature.sales.detail

import me.busta.barksaccountant.model.Sale

data class SaleDetailState(
    val sale: Sale? = null,
    val isLoading: Boolean = false,
    val showPayConfirm: Boolean = false,
    val showDeliverConfirm: Boolean = false,
    val error: String? = null
)
