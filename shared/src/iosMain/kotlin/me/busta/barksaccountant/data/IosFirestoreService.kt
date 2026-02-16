package me.busta.barksaccountant.data

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Bridge interface that the iOS (Swift) app must implement.
 * Kotlin exports this as an Objective-C protocol.
 * Swift conforms to this protocol using the native Firebase iOS SDK.
 */
interface FirestoreServiceBridge {
    fun getDocument(
        collection: String,
        documentId: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onError: (String) -> Unit
    )

    fun getDocuments(
        collection: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    )

    fun setDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    fun deleteDocument(
        collection: String,
        documentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

class IosFirestoreService(
    private val bridge: FirestoreServiceBridge
) : FirestoreService {

    override suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? =
        suspendCoroutine { continuation ->
            bridge.getDocument(
                collection = collection,
                documentId = documentId,
                onSuccess = { continuation.resume(it) },
                onError = { continuation.resumeWithException(Exception(it)) }
            )
        }

    override suspend fun getDocuments(collection: String): List<Map<String, Any>> =
        suspendCoroutine { continuation ->
            bridge.getDocuments(
                collection = collection,
                onSuccess = { continuation.resume(it) },
                onError = { continuation.resumeWithException(Exception(it)) }
            )
        }

    override suspend fun setDocument(collection: String, documentId: String, data: Map<String, Any>) {
        suspendCoroutine { continuation ->
            bridge.setDocument(
                collection = collection,
                documentId = documentId,
                data = data,
                onSuccess = { continuation.resume(Unit) },
                onError = { continuation.resumeWithException(Exception(it)) }
            )
        }
    }

    override suspend fun deleteDocument(collection: String, documentId: String) {
        suspendCoroutine { continuation ->
            bridge.deleteDocument(
                collection = collection,
                documentId = documentId,
                onSuccess = { continuation.resume(Unit) },
                onError = { continuation.resumeWithException(Exception(it)) }
            )
        }
    }
}
