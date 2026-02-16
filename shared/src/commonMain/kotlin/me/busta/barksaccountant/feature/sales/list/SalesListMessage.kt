package me.busta.barksaccountant.feature.sales.list

import me.busta.barksaccountant.model.Sale

sealed interface SalesListMessage {
    data object Started : SalesListMessage
    data class SalesLoaded(val sales: List<Sale>) : SalesListMessage
    data class ErrorOccurred(val error: String) : SalesListMessage
}
