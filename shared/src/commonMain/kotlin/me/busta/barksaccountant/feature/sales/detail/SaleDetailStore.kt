package me.busta.barksaccountant.feature.sales.detail

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.busta.barksaccountant.data.repository.SaleRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class SaleDetailStore(
    private val saleRepository: SaleRepository
) : Store<SaleDetailState, SaleDetailMessage, SaleDetailEffect>(SaleDetailState()) {

    override fun reduce(state: SaleDetailState, message: SaleDetailMessage): Next<SaleDetailState, SaleDetailEffect> {
        return when (message) {
            is SaleDetailMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                SaleDetailEffect.LoadSale(message.saleId)
            )
            is SaleDetailMessage.SaleLoaded -> Next.just(
                state.copy(sale = message.sale, isLoading = false, error = null)
            )
            is SaleDetailMessage.MarkAsPaidTapped -> Next.just(
                state.copy(showPayConfirm = true)
            )
            is SaleDetailMessage.ConfirmPaid -> {
                val sale = state.sale ?: return Next.just(state)
                Next.withEffects(
                    state.copy(showPayConfirm = false, isLoading = true),
                    SaleDetailEffect.SetPaid(sale.id)
                )
            }
            is SaleDetailMessage.MarkAsDeliveredTapped -> Next.just(
                state.copy(showDeliverConfirm = true)
            )
            is SaleDetailMessage.ConfirmDelivered -> {
                val sale = state.sale ?: return Next.just(state)
                Next.withEffects(
                    state.copy(showDeliverConfirm = false, isLoading = true),
                    SaleDetailEffect.SetDelivered(sale.id)
                )
            }
            is SaleDetailMessage.SaleUpdated -> Next.just(
                state.copy(sale = message.sale, isLoading = false, error = null)
            )
            is SaleDetailMessage.DismissConfirm -> Next.just(
                state.copy(showPayConfirm = false, showDeliverConfirm = false)
            )
            is SaleDetailMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: SaleDetailEffect) {
        when (effect) {
            is SaleDetailEffect.LoadSale -> {
                try {
                    val sale = saleRepository.getSale(effect.saleId)
                    if (sale != null) {
                        dispatch(SaleDetailMessage.SaleLoaded(sale))
                    } else {
                        dispatch(SaleDetailMessage.ErrorOccurred("Venta no encontrada"))
                    }
                } catch (e: Exception) {
                    dispatch(SaleDetailMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is SaleDetailEffect.SetPaid -> {
                try {
                    val sale = saleRepository.getSale(effect.saleId) ?: throw Exception("Venta no encontrada")
                    val updated = sale.copy(isPaid = true)
                    saleRepository.updateSale(updated)
                    dispatch(SaleDetailMessage.SaleUpdated(updated))
                } catch (e: Exception) {
                    dispatch(SaleDetailMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is SaleDetailEffect.SetDelivered -> {
                try {
                    val sale = saleRepository.getSale(effect.saleId) ?: throw Exception("Venta no encontrada")
                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date.toString()
                    val updated = sale.copy(isDelivered = true, deliveryDate = today)
                    saleRepository.updateSale(updated)
                    dispatch(SaleDetailMessage.SaleUpdated(updated))
                } catch (e: Exception) {
                    dispatch(SaleDetailMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
