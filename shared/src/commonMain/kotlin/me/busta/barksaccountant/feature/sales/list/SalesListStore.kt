package me.busta.barksaccountant.feature.sales.list

import me.busta.barksaccountant.data.repository.SaleRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class SalesListStore(
    private val saleRepository: SaleRepository
) : Store<SalesListState, SalesListMessage, SalesListEffect>(SalesListState()) {

    override fun reduce(state: SalesListState, message: SalesListMessage): Next<SalesListState, SalesListEffect> {
        return when (message) {
            is SalesListMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                SalesListEffect.LoadSales
            )
            is SalesListMessage.SalesLoaded -> Next.just(
                state.copy(sales = message.sales, isLoading = false, error = null)
            )
            is SalesListMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: SalesListEffect) {
        when (effect) {
            is SalesListEffect.LoadSales -> {
                try {
                    val sales = saleRepository.getSales()
                    dispatch(SalesListMessage.SalesLoaded(sales))
                } catch (e: Exception) {
                    dispatch(SalesListMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
