package me.busta.barksaccountant.feature.settings.clients.list

import me.busta.barksaccountant.data.repository.ClientRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class ClientsListStore(
    private val clientRepository: ClientRepository
) : Store<ClientsListState, ClientsListMessage, ClientsListEffect>(ClientsListState()) {

    override fun reduce(state: ClientsListState, message: ClientsListMessage): Next<ClientsListState, ClientsListEffect> {
        return when (message) {
            is ClientsListMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                ClientsListEffect.LoadClients
            )
            is ClientsListMessage.ClientsLoaded -> Next.just(
                state.copy(clients = message.clients, isLoading = false, error = null)
            )
            is ClientsListMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: ClientsListEffect) {
        when (effect) {
            is ClientsListEffect.LoadClients -> {
                try {
                    val clients = clientRepository.getClients()
                    dispatch(ClientsListMessage.ClientsLoaded(clients))
                } catch (e: Exception) {
                    dispatch(ClientsListMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
