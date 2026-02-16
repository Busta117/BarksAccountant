package me.busta.barksaccountant.data

interface FirestoreService {
    suspend fun getDocument(collection: String, documentId: String): Map<String, Any>?
    suspend fun getDocuments(collection: String): List<Map<String, Any>>
    suspend fun setDocument(collection: String, documentId: String, data: Map<String, Any>)
    suspend fun deleteDocument(collection: String, documentId: String)
}
