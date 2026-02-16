package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.Client

interface ClientRepository {
    suspend fun getClients(): List<Client>
    suspend fun getClient(id: String): Client?
    suspend fun saveClient(client: Client): Client
    suspend fun updateClient(client: Client): Client
}
