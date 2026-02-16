package me.busta.barksaccountant.feature.stats

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.busta.barksaccountant.data.repository.PurchaseRepository
import me.busta.barksaccountant.data.repository.SaleRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class StatsStore(
    private val saleRepository: SaleRepository,
    private val purchaseRepository: PurchaseRepository
) : Store<StatsState, StatsMessage, StatsEffect>(StatsState()) {

    override fun reduce(state: StatsState, message: StatsMessage): Next<StatsState, StatsEffect> {
        return when (message) {
            is StatsMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                StatsEffect.LoadData
            )
            is StatsMessage.DataLoaded -> {
                val currentYear = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .year

                val allDates = message.sales.map { it.orderDate } + message.purchases.map { it.date }
                val years = allDates
                    .mapNotNull { if (it.length >= 4) it.substring(0, 4).toIntOrNull() else null }
                    .distinct()
                    .sortedDescending()

                val defaultYear = if (currentYear in years) currentYear else (years.firstOrNull() ?: currentYear)

                Next.just(
                    state.copy(
                        allSales = message.sales,
                        allPurchases = message.purchases,
                        availableYears = years,
                        selectedYear = if (state.selectedYear == 0) defaultYear else state.selectedYear,
                        isLoading = false,
                        error = null
                    )
                )
            }
            is StatsMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
            is StatsMessage.YearSelected -> Next.just(
                state.copy(selectedYear = message.year, selectedMonth = null)
            )
            is StatsMessage.MonthSelected -> Next.just(
                state.copy(selectedMonth = message.month)
            )
        }
    }

    override suspend fun handleEffect(effect: StatsEffect) {
        when (effect) {
            is StatsEffect.LoadData -> {
                try {
                    val sales = saleRepository.getSales()
                    val purchases = purchaseRepository.getPurchases()
                    dispatch(StatsMessage.DataLoaded(sales = sales, purchases = purchases))
                } catch (e: Exception) {
                    dispatch(StatsMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
