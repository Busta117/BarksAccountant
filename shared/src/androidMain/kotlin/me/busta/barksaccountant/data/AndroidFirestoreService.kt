package me.busta.barksaccountant.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidFirestoreService : FirestoreService {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? {
        val snapshot = db.collection(collection).document(documentId).get().await()
        return snapshot.data
    }

    override suspend fun setDocument(collection: String, documentId: String, data: Map<String, Any>) {
        db.collection(collection).document(documentId).set(data).await()
    }

    override suspend fun deleteDocument(collection: String, documentId: String) {
        db.collection(collection).document(documentId).delete().await()
    }
}
