package me.busta.barksaccountant.feature.stats

import me.busta.barksaccountant.model.Purchase
import me.busta.barksaccountant.model.Sale

sealed interface StatsMessage {
    data object Started : StatsMessage
    data class DataLoaded(
        val sales: List<Sale>,
        val purchases: List<Purchase>
    ) : StatsMessage
    data class ErrorOccurred(val error: String) : StatsMessage
    data class YearSelected(val year: Int) : StatsMessage
    data class MonthSelected(val month: Int?) : StatsMessage
}
