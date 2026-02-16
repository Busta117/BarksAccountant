package me.busta.barksaccountant.feature.purchases.list

import me.busta.barksaccountant.data.repository.PurchaseRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class PurchasesListStore(
    private val purchaseRepository: PurchaseRepository
) : Store<PurchasesListState, PurchasesListMessage, PurchasesListEffect>(PurchasesListState()) {

    override fun reduce(state: PurchasesListState, message: PurchasesListMessage): Next<PurchasesListState, PurchasesListEffect> {
        return when (message) {
            is PurchasesListMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                PurchasesListEffect.LoadPurchases
            )
            is PurchasesListMessage.PurchasesLoaded -> Next.just(
                state.copy(purchases = message.purchases, isLoading = false, error = null)
            )
            is PurchasesListMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: PurchasesListEffect) {
        when (effect) {
            is PurchasesListEffect.LoadPurchases -> {
                try {
                    val purchases = purchaseRepository.getPurchases()
                    dispatch(PurchasesListMessage.PurchasesLoaded(purchases))
                } catch (e: Exception) {
                    dispatch(PurchasesListMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
