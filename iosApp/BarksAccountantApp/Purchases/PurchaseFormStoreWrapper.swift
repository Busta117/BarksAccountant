import Foundation
import Shared

@Observable
final class PurchaseFormStoreWrapper {
    private(set) var isEditing: Bool = false
    private(set) var title: String = ""
    private(set) var purchaseDescription: String = ""
    private(set) var value: String = ""
    private(set) var date: String = ""
    private(set) var isSaving: Bool = false
    private(set) var savedSuccessfully: Bool = false
    private(set) var showDeleteConfirm: Bool = false
    private(set) var deletedSuccessfully: Bool = false
    private(set) var error: String? = nil
    private(set) var canSave: Bool = false

    private let store: PurchaseFormStore
    private var collector: FlowCollector<PurchaseFormState>?

    init(purchaseRepository: PurchaseRepository, personName: String) {
        self.store = PurchaseFormStore(purchaseRepository: purchaseRepository, createdBy: personName)
    }

    func start(purchaseId: String?) {
        collector = FlowCollector<PurchaseFormState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.isEditing = state.isEditing
                self.title = state.title
                self.purchaseDescription = state.description_
                self.value = state.value
                self.date = state.date
                self.isSaving = state.isSaving
                self.savedSuccessfully = state.savedSuccessfully
                self.showDeleteConfirm = state.showDeleteConfirm
                self.deletedSuccessfully = state.deletedSuccessfully
                self.error = state.error
                self.canSave = state.canSave
            }
        )
        store.dispatch(message: PurchaseFormMessageStarted(purchaseId: purchaseId))
    }

    func titleChanged(_ text: String) { store.dispatch(message: PurchaseFormMessageTitleChanged(text: text)) }
    func descriptionChanged(_ text: String) { store.dispatch(message: PurchaseFormMessageDescriptionChanged(text: text)) }
    func valueChanged(_ text: String) { store.dispatch(message: PurchaseFormMessageValueChanged(text: text)) }
    func dateChanged(_ date: String) { store.dispatch(message: PurchaseFormMessageDateChanged(date: date)) }
    func saveTapped() { store.dispatch(message: PurchaseFormMessageSaveTapped.shared) }
    func deleteTapped() { store.dispatch(message: PurchaseFormMessageDeleteTapped.shared) }
    func confirmDelete() { store.dispatch(message: PurchaseFormMessageConfirmDelete.shared) }
    func dismissDelete() { store.dispatch(message: PurchaseFormMessageDismissDelete.shared) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
