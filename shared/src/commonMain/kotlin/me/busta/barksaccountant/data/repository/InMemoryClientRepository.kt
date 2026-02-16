package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Client
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class InMemoryClientRepository : ClientRepository {
    private val clients = mutableListOf(
        Client(id = "c1", name = "Juan García", responsible = "María", nif = "12345678A", address = "Calle Mayor 1"),
        Client(id = "c2", name = "Ana López", responsible = null, nif = null, address = "Av. Libertad 23"),
        Client(id = "c3", name = "Pedro Martínez", responsible = "Carlos", nif = "87654321B", address = null)
    )

    override suspend fun getClients(): List<Client> {
        return clients.toList()
    }

    override suspend fun getClient(id: String): Client? {
        return clients.find { it.id == id }
    }

    override suspend fun saveClient(client: Client): Client {
        val newClient = client.copy(id = Uuid.random().toString())
        clients.add(newClient)
        return newClient
    }

    override suspend fun updateClient(client: Client): Client {
        val index = clients.indexOfFirst { it.id == client.id }
        if (index >= 0) {
            clients[index] = client
        }
        return client
    }
}
