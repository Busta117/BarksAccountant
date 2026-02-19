package me.busta.barksaccountant.feature.settings.clients.form

import me.busta.barksaccountant.data.repository.ClientRepository
import me.busta.barksaccountant.model.Client
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ClientFormStore(
    private val clientRepository: ClientRepository
) : Store<ClientFormState, ClientFormMessage, ClientFormEffect>(ClientFormState()) {

    override fun reduce(state: ClientFormState, message: ClientFormMessage): Next<ClientFormState, ClientFormEffect> {
        return when (message) {
            is ClientFormMessage.Started -> Next.withEffects(
                state.copy(clientId = message.clientId, isEditing = message.clientId != null),
                ClientFormEffect.LoadClient(message.clientId)
            )
            is ClientFormMessage.ClientLoaded -> {
                val client = message.client
                if (client != null) {
                    Next.just(state.copy(
                        name = client.name,
                        responsible = client.responsible ?: "",
                        nif = client.nif ?: "",
                        address = client.address ?: "",
                        ivaPct = client.ivaPct?.toString() ?: "",
                        recargoPct = client.recargoPct?.toString() ?: ""
                    ))
                } else {
                    Next.just(state)
                }
            }
            is ClientFormMessage.NameChanged -> Next.just(state.copy(name = message.text))
            is ClientFormMessage.ResponsibleChanged -> Next.just(state.copy(responsible = message.text))
            is ClientFormMessage.NifChanged -> Next.just(state.copy(nif = message.text))
            is ClientFormMessage.AddressChanged -> Next.just(state.copy(address = message.text))
            is ClientFormMessage.IvaPctChanged -> Next.just(state.copy(ivaPct = message.text))
            is ClientFormMessage.RecargoPctChanged -> Next.just(state.copy(recargoPct = message.text))
            is ClientFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val client = Client(
                    id = state.clientId ?: Uuid.random().toString(),
                    name = state.name,
                    responsible = state.responsible.ifBlank { null },
                    nif = state.nif.ifBlank { null },
                    address = state.address.ifBlank { null },
                    ivaPct = state.ivaPct.toDoubleOrNull(),
                    recargoPct = state.recargoPct.toDoubleOrNull()
                )
                if (state.isEditing) {
                    Next.withEffects(state.copy(isSaving = true, error = null), ClientFormEffect.UpdateClient(client))
                } else {
                    Next.withEffects(state.copy(isSaving = true, error = null), ClientFormEffect.SaveClient(client))
                }
            }
            is ClientFormMessage.SaveSuccess -> Next.just(state.copy(isSaving = false, savedSuccessfully = true))
            is ClientFormMessage.DeleteTapped -> Next.just(state.copy(showDeleteConfirm = true))
            is ClientFormMessage.ConfirmDelete -> {
                val id = state.clientId ?: return Next.just(state)
                Next.withEffects(state.copy(showDeleteConfirm = false, isSaving = true), ClientFormEffect.DeleteClient(id))
            }
            is ClientFormMessage.DismissDelete -> Next.just(state.copy(showDeleteConfirm = false))
            is ClientFormMessage.DeleteSuccess -> Next.just(state.copy(isSaving = false, deletedSuccessfully = true))
            is ClientFormMessage.ErrorOccurred -> Next.just(state.copy(isSaving = false, error = message.error))
        }
    }

    override suspend fun handleEffect(effect: ClientFormEffect) {
        when (effect) {
            is ClientFormEffect.LoadClient -> {
                try {
                    val client = effect.clientId?.let { clientRepository.getClient(it) }
                    dispatch(ClientFormMessage.ClientLoaded(client))
                } catch (e: Exception) {
                    dispatch(ClientFormMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is ClientFormEffect.SaveClient -> {
                try {
                    clientRepository.saveClient(effect.client)
                    dispatch(ClientFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(ClientFormMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
            is ClientFormEffect.UpdateClient -> {
                try {
                    clientRepository.updateClient(effect.client)
                    dispatch(ClientFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(ClientFormMessage.ErrorOccurred(e.message ?: "Error al actualizar"))
                }
            }
            is ClientFormEffect.DeleteClient -> {
                try {
                    clientRepository.deleteClient(effect.id)
                    dispatch(ClientFormMessage.DeleteSuccess)
                } catch (e: Exception) {
                    dispatch(ClientFormMessage.ErrorOccurred(e.message ?: "Error al eliminar"))
                }
            }
        }
    }
}
