package me.busta.barksaccountant.feature.sales.detail

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.busta.barksaccountant.data.repository.BusinessInfoRepository
import me.busta.barksaccountant.data.repository.ClientRepository
import me.busta.barksaccountant.data.repository.SaleRepository
import me.busta.barksaccountant.model.BusinessInfo
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import me.busta.barksaccountant.util.InvoiceGenerator

class SaleDetailStore(
    private val saleRepository: SaleRepository,
    private val clientRepository: ClientRepository,
    private val businessInfoRepository: BusinessInfoRepository
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
            is SaleDetailMessage.ExportTapped -> {
                val sale = state.sale ?: return Next.just(state)
                Next.withEffects(
                    state.copy(isGeneratingInvoice = true),
                    SaleDetailEffect.GenerateInvoice(sale.id)
                )
            }
            is SaleDetailMessage.InvoiceGenerated -> Next.just(
                state.copy(invoiceHtml = message.html, isGeneratingInvoice = false)
            )
            is SaleDetailMessage.InvoiceDismissed -> Next.just(
                state.copy(invoiceHtml = null)
            )
            is SaleDetailMessage.ShareSummaryTapped -> {
                val sale = state.sale ?: return Next.just(state)
                Next.withEffects(
                    state.copy(isGeneratingSummary = true),
                    SaleDetailEffect.GenerateOrderSummary(sale.id)
                )
            }
            is SaleDetailMessage.SummaryGenerated -> Next.just(
                state.copy(summaryHtml = message.html, isGeneratingSummary = false)
            )
            is SaleDetailMessage.SummaryDismissed -> Next.just(
                state.copy(summaryHtml = null)
            )
            is SaleDetailMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, isGeneratingInvoice = false, isGeneratingSummary = false, error = message.error)
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
            is SaleDetailEffect.GenerateInvoice -> {
                try {
                    val sale = saleRepository.getSale(effect.saleId)
                        ?: throw Exception("Venta no encontrada")
                    val clients = clientRepository.getClients()
                    val client = clients.find { it.name == sale.clientName }
                    val businessInfo = businessInfoRepository.getBusinessInfo()
                        ?: BusinessInfo(
                            businessName = "Sin configurar",
                            nif = null,
                            address = null,
                            phone = null,
                            email = null,
                            bankName = null,
                            iban = null,
                            bankHolder = null
                        )
                    val html = InvoiceGenerator.generateHtml(sale, client, businessInfo)
                    dispatch(SaleDetailMessage.InvoiceGenerated(html))
                } catch (e: Exception) {
                    dispatch(SaleDetailMessage.ErrorOccurred(e.message ?: "Error al generar factura"))
                }
            }
            is SaleDetailEffect.GenerateOrderSummary -> {
                try {
                    val sale = saleRepository.getSale(effect.saleId)
                        ?: throw Exception("Venta no encontrada")
                    val clients = clientRepository.getClients()
                    val client = clients.find { it.name == sale.clientName }
                    val businessInfo = businessInfoRepository.getBusinessInfo()
                        ?: BusinessInfo(
                            businessName = "Sin configurar",
                            nif = null,
                            address = null,
                            phone = null,
                            email = null,
                            bankName = null,
                            iban = null,
                            bankHolder = null
                        )
                    val html = InvoiceGenerator.generateOrderSummaryHtml(sale, client, businessInfo)
                    dispatch(SaleDetailMessage.SummaryGenerated(html))
                } catch (e: Exception) {
                    dispatch(SaleDetailMessage.ErrorOccurred(e.message ?: "Error al generar resumen"))
                }
            }
        }
    }
}
