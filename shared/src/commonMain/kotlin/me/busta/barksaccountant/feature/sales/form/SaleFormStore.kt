package me.busta.barksaccountant.feature.sales.form

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.busta.barksaccountant.data.repository.ClientRepository
import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.data.repository.SaleRepository
import me.busta.barksaccountant.model.Sale
import me.busta.barksaccountant.model.SaleProduct
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SaleFormStore(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val clientRepository: ClientRepository,
    private val createdBy: String = ""
) : Store<SaleFormState, SaleFormMessage, SaleFormEffect>(SaleFormState()) {

    override fun reduce(state: SaleFormState, message: SaleFormMessage): Next<SaleFormState, SaleFormEffect> {
        return when (message) {
            is SaleFormMessage.Started -> {
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date.toString()
                Next.withEffects(
                    state.copy(
                        saleId = message.saleId,
                        isEditing = message.saleId != null,
                        orderDate = today
                    ),
                    SaleFormEffect.LoadFormData(message.saleId)
                )
            }
            is SaleFormMessage.DataLoaded -> {
                val sale = message.sale
                if (sale != null) {
                    Next.just(
                        state.copy(
                            clientName = sale.clientName,
                            responsible = sale.responsible ?: "",
                            orderDate = sale.orderDate,
                            deliveryDate = sale.deliveryDate,
                            products = sale.products,
                            clients = message.clients,
                            availableProducts = message.products
                        )
                    )
                } else {
                    Next.just(
                        state.copy(
                            clients = message.clients,
                            availableProducts = message.products
                        )
                    )
                }
            }
            is SaleFormMessage.ClientSelected -> Next.just(
                state.copy(clientName = message.name)
            )
            is SaleFormMessage.ResponsibleChanged -> Next.just(
                state.copy(responsible = message.text)
            )
            is SaleFormMessage.OrderDateChanged -> Next.just(
                state.copy(orderDate = message.date)
            )
            is SaleFormMessage.DeliveryDateChanged -> Next.just(
                state.copy(deliveryDate = message.date)
            )
            is SaleFormMessage.AddProduct -> {
                val existing = state.products.indexOfFirst { it.productId == message.product.id }
                val updatedProducts = if (existing >= 0) {
                    state.products.toMutableList().apply {
                        this[existing] = this[existing].copy(quantity = this[existing].quantity + 1)
                    }
                } else {
                    state.products + SaleProduct(
                        productId = message.product.id,
                        name = message.product.name,
                        unitPrice = message.product.unitPrice,
                        quantity = 1
                    )
                }
                Next.just(state.copy(products = updatedProducts))
            }
            is SaleFormMessage.RemoveProduct -> {
                val updatedProducts = state.products.toMutableList().apply {
                    removeAt(message.index)
                }
                Next.just(state.copy(products = updatedProducts))
            }
            is SaleFormMessage.IncrementQuantity -> {
                val updatedProducts = state.products.toMutableList().apply {
                    this[message.index] = this[message.index].copy(
                        quantity = this[message.index].quantity + 1
                    )
                }
                Next.just(state.copy(products = updatedProducts))
            }
            is SaleFormMessage.DecrementQuantity -> {
                val product = state.products[message.index]
                if (product.quantity <= 1) {
                    val updatedProducts = state.products.toMutableList().apply {
                        removeAt(message.index)
                    }
                    Next.just(state.copy(products = updatedProducts))
                } else {
                    val updatedProducts = state.products.toMutableList().apply {
                        this[message.index] = this[message.index].copy(
                            quantity = this[message.index].quantity - 1
                        )
                    }
                    Next.just(state.copy(products = updatedProducts))
                }
            }
            is SaleFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val sale = Sale(
                    id = state.saleId ?: Uuid.random().toString(),
                    clientName = state.clientName,
                    responsible = state.responsible.ifBlank { null },
                    orderDate = state.orderDate,
                    deliveryDate = state.deliveryDate,
                    products = state.products,
                    totalPrice = state.totalPrice,
                    isPaid = false,
                    isDelivered = false,
                    createdBy = createdBy
                )
                if (state.isEditing) {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        SaleFormEffect.UpdateSale(sale)
                    )
                } else {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        SaleFormEffect.SaveSale(sale)
                    )
                }
            }
            is SaleFormMessage.SaveSuccess -> Next.just(
                state.copy(isSaving = false, savedSuccessfully = true)
            )
            is SaleFormMessage.ErrorOccurred -> Next.just(
                state.copy(isSaving = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: SaleFormEffect) {
        when (effect) {
            is SaleFormEffect.LoadFormData -> {
                try {
                    val clients = clientRepository.getClients()
                    val products = productRepository.getProducts()
                    val sale = effect.saleId?.let { saleRepository.getSale(it) }
                    dispatch(SaleFormMessage.DataLoaded(clients, products, sale))
                } catch (e: Exception) {
                    dispatch(SaleFormMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is SaleFormEffect.SaveSale -> {
                try {
                    saleRepository.saveSale(effect.sale)
                    dispatch(SaleFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(SaleFormMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
            is SaleFormEffect.UpdateSale -> {
                try {
                    saleRepository.updateSale(effect.sale)
                    dispatch(SaleFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(SaleFormMessage.ErrorOccurred(e.message ?: "Error al actualizar"))
                }
            }
        }
    }
}
