package me.busta.barksaccountant.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidFirestoreService : FirestoreService {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? {
        val snapshot = db.collection(collection).document(documentId).get().await()
        return if (snapshot.exists()) snapshot.data else null
    }

    override suspend fun getDocuments(collection: String): List<Map<String, Any>> {
        val snapshot = db.collection(collection).get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.toMutableMap()?.apply { put("id", doc.id) }
        }
    }

    override suspend fun setDocument(collection: String, documentId: String, data: Map<String, Any>) {
        db.collection(collection).document(documentId).set(data).await()
    }

    override suspend fun deleteDocument(collection: String, documentId: String) {
        db.collection(collection).document(documentId).delete().await()
    }
}
