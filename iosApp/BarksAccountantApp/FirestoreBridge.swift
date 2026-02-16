import Foundation
import Shared

/// Stub implementation of the Kotlin FirestoreServiceBridge protocol.
/// TODO: Replace with real Firebase iOS SDK calls once FirebaseFirestore is added via SPM.
final class FirestoreBridge: FirestoreServiceBridge {
    func getDocument(
        collection: String,
        documentId: String,
        onSuccess: @escaping (([String: Any]?) -> Void),
        onError: @escaping ((String) -> Void)
    ) {
        // TODO: Use Firestore.firestore().collection(collection).document(documentId).getDocument()
        onSuccess(nil)
    }

    func setDocument(
        collection: String,
        documentId: String,
        data: [String: Any],
        onSuccess: @escaping (() -> Void),
        onError: @escaping ((String) -> Void)
    ) {
        // TODO: Use Firestore.firestore().collection(collection).document(documentId).setData()
        onSuccess()
    }

    func deleteDocument(
        collection: String,
        documentId: String,
        onSuccess: @escaping (() -> Void),
        onError: @escaping ((String) -> Void)
    ) {
        // TODO: Use Firestore.firestore().collection(collection).document(documentId).delete()
        onSuccess()
    }
}
