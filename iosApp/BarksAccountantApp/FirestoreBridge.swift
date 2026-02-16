import Foundation
import Shared
import FirebaseFirestore

final class FirestoreBridge: FirestoreServiceBridge {
    private lazy var db = Firestore.firestore()

    func getDocument(
        collection: String,
        documentId: String,
        onSuccess: @escaping (([String: Any]?) -> Void),
        onError: @escaping ((String) -> Void)
    ) {
        db.collection(collection).document(documentId).getDocument { snapshot, error in
            if let error = error {
                onError(error.localizedDescription)
                return
            }
            guard let snapshot = snapshot, snapshot.exists else {
                onSuccess(nil)
                return
            }
            onSuccess(snapshot.data())
        }
    }

    func getDocuments(
        collection: String,
        onSuccess: @escaping (([[String: Any]]) -> Void),
        onError: @escaping ((String) -> Void)
    ) {
        db.collection(collection).getDocuments { snapshot, error in
            if let error = error {
                onError(error.localizedDescription)
                return
            }
            guard let snapshot = snapshot else {
                onSuccess([])
                return
            }
            let results: [[String: Any]] = snapshot.documents.map { doc in
                var data = doc.data()
                data["id"] = doc.documentID
                return data
            }
            onSuccess(results)
        }
    }

    func setDocument(
        collection: String,
        documentId: String,
        data: [String: Any],
        onSuccess: @escaping (() -> Void),
        onError: @escaping ((String) -> Void)
    ) {
        db.collection(collection).document(documentId).setData(data) { error in
            if let error = error {
                onError(error.localizedDescription)
                return
            }
            onSuccess()
        }
    }

    func deleteDocument(
        collection: String,
        documentId: String,
        onSuccess: @escaping (() -> Void),
        onError: @escaping ((String) -> Void)
    ) {
        db.collection(collection).document(documentId).delete { error in
            if let error = error {
                onError(error.localizedDescription)
                return
            }
            onSuccess()
        }
    }
}
