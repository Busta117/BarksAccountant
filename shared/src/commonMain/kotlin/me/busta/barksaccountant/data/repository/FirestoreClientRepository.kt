package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.data.FirestoreService
import me.busta.barksaccountant.model.Client
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FirestoreClientRepository(
    private val firestoreService: FirestoreService,
    private val appId: String
) : ClientRepository {

    private val collectionPath get() = "apps/$appId/clients"

    override suspend fun getClients(): List<Client> {
        return firestoreService.getDocuments(collectionPath).map { mapToClient(it) }
    }

    override suspend fun getClient(id: String): Client? {
        val data = firestoreService.getDocument(collectionPath, id) ?: return null
        return mapToClient(data, id)
    }

    override suspend fun saveClient(client: Client): Client {
        val newId = Uuid.random().toString()
        val newClient = client.copy(id = newId)
        firestoreService.setDocument(collectionPath, newId, clientToMap(newClient))
        return newClient
    }

    override suspend fun updateClient(client: Client): Client {
        firestoreService.setDocument(collectionPath, client.id, clientToMap(client))
        return client
    }

    override suspend fun deleteClient(id: String) {
        firestoreService.deleteDocument(collectionPath, id)
    }

    private fun clientToMap(client: Client): Map<String, Any> {
        val map = mutableMapOf<String, Any>("name" to client.name)
        client.responsible?.let { map["responsible"] = it }
        client.nif?.let { map["nif"] = it }
        client.address?.let { map["address"] = it }
        return map
    }

    private fun mapToClient(data: Map<String, Any>, overrideId: String? = null): Client {
        return Client(
            id = overrideId ?: (data["id"] as? String ?: ""),
            name = data["name"] as? String ?: "",
            responsible = data["responsible"] as? String,
            nif = data["nif"] as? String,
            address = data["address"] as? String
        )
    }
}
