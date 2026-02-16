package me.busta.barksaccountant.feature.purchases.form

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.busta.barksaccountant.data.repository.PurchaseRepository
import me.busta.barksaccountant.model.Purchase
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class PurchaseFormStore(
    private val purchaseRepository: PurchaseRepository,
    private val createdBy: String = ""
) : Store<PurchaseFormState, PurchaseFormMessage, PurchaseFormEffect>(PurchaseFormState()) {

    override fun reduce(state: PurchaseFormState, message: PurchaseFormMessage): Next<PurchaseFormState, PurchaseFormEffect> {
        return when (message) {
            is PurchaseFormMessage.Started -> {
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date.toString()
                Next.withEffects(
                    state.copy(
                        purchaseId = message.purchaseId,
                        isEditing = message.purchaseId != null,
                        date = today
                    ),
                    PurchaseFormEffect.LoadPurchase(message.purchaseId)
                )
            }
            is PurchaseFormMessage.PurchaseLoaded -> {
                val purchase = message.purchase
                if (purchase != null) {
                    Next.just(
                        state.copy(
                            title = purchase.title,
                            description = purchase.description ?: "",
                            value = purchase.value.toString(),
                            date = purchase.date
                        )
                    )
                } else {
                    Next.just(state)
                }
            }
            is PurchaseFormMessage.TitleChanged -> Next.just(state.copy(title = message.text))
            is PurchaseFormMessage.DescriptionChanged -> Next.just(state.copy(description = message.text))
            is PurchaseFormMessage.ValueChanged -> Next.just(state.copy(value = message.text))
            is PurchaseFormMessage.DateChanged -> Next.just(state.copy(date = message.date))
            is PurchaseFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val purchase = Purchase(
                    id = state.purchaseId ?: Uuid.random().toString(),
                    title = state.title,
                    description = state.description.ifBlank { null },
                    value = state.value.toDouble(),
                    date = state.date,
                    createdBy = createdBy
                )
                if (state.isEditing) {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        PurchaseFormEffect.UpdatePurchase(purchase)
                    )
                } else {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        PurchaseFormEffect.SavePurchase(purchase)
                    )
                }
            }
            is PurchaseFormMessage.SaveSuccess -> Next.just(
                state.copy(isSaving = false, savedSuccessfully = true)
            )
            is PurchaseFormMessage.DeleteTapped -> Next.just(
                state.copy(showDeleteConfirm = true)
            )
            is PurchaseFormMessage.ConfirmDelete -> {
                val id = state.purchaseId ?: return Next.just(state)
                Next.withEffects(
                    state.copy(showDeleteConfirm = false, isSaving = true),
                    PurchaseFormEffect.DeletePurchase(id)
                )
            }
            is PurchaseFormMessage.DismissDelete -> Next.just(
                state.copy(showDeleteConfirm = false)
            )
            is PurchaseFormMessage.DeleteSuccess -> Next.just(
                state.copy(isSaving = false, deletedSuccessfully = true)
            )
            is PurchaseFormMessage.ErrorOccurred -> Next.just(
                state.copy(isSaving = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: PurchaseFormEffect) {
        when (effect) {
            is PurchaseFormEffect.LoadPurchase -> {
                try {
                    val purchase = effect.purchaseId?.let { purchaseRepository.getPurchase(it) }
                    dispatch(PurchaseFormMessage.PurchaseLoaded(purchase))
                } catch (e: Exception) {
                    dispatch(PurchaseFormMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is PurchaseFormEffect.SavePurchase -> {
                try {
                    purchaseRepository.savePurchase(effect.purchase)
                    dispatch(PurchaseFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(PurchaseFormMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
            is PurchaseFormEffect.UpdatePurchase -> {
                try {
                    purchaseRepository.updatePurchase(effect.purchase)
                    dispatch(PurchaseFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(PurchaseFormMessage.ErrorOccurred(e.message ?: "Error al actualizar"))
                }
            }
            is PurchaseFormEffect.DeletePurchase -> {
                try {
                    purchaseRepository.deletePurchase(effect.id)
                    dispatch(PurchaseFormMessage.DeleteSuccess)
                } catch (e: Exception) {
                    dispatch(PurchaseFormMessage.ErrorOccurred(e.message ?: "Error al eliminar"))
                }
            }
        }
    }
}
