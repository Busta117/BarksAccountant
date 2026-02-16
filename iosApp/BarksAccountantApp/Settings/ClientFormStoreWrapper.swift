import Foundation
import Shared

@Observable
final class ClientFormStoreWrapper {
    private(set) var isEditing: Bool = false
    private(set) var name: String = ""
    private(set) var responsible: String = ""
    private(set) var nif: String = ""
    private(set) var address: String = ""
    private(set) var isSaving: Bool = false
    private(set) var savedSuccessfully: Bool = false
    private(set) var showDeleteConfirm: Bool = false
    private(set) var deletedSuccessfully: Bool = false
    private(set) var error: String? = nil
    private(set) var canSave: Bool = false

    private let store: ClientFormStore
    private var collector: FlowCollector<ClientFormState>?

    init(clientRepository: ClientRepository) {
        self.store = ClientFormStore(clientRepository: clientRepository)
    }

    func start(clientId: String?) {
        collector = FlowCollector<ClientFormState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.isEditing = state.isEditing
                self.name = state.name
                self.responsible = state.responsible
                self.nif = state.nif
                self.address = state.address
                self.isSaving = state.isSaving
                self.savedSuccessfully = state.savedSuccessfully
                self.showDeleteConfirm = state.showDeleteConfirm
                self.deletedSuccessfully = state.deletedSuccessfully
                self.error = state.error
                self.canSave = state.canSave
            }
        )
        store.dispatch(message: ClientFormMessageStarted(clientId: clientId))
    }

    func nameChanged(_ text: String) { store.dispatch(message: ClientFormMessageNameChanged(text: text)) }
    func responsibleChanged(_ text: String) { store.dispatch(message: ClientFormMessageResponsibleChanged(text: text)) }
    func nifChanged(_ text: String) { store.dispatch(message: ClientFormMessageNifChanged(text: text)) }
    func addressChanged(_ text: String) { store.dispatch(message: ClientFormMessageAddressChanged(text: text)) }
    func saveTapped() { store.dispatch(message: ClientFormMessageSaveTapped.shared) }
    func deleteTapped() { store.dispatch(message: ClientFormMessageDeleteTapped.shared) }
    func confirmDelete() { store.dispatch(message: ClientFormMessageConfirmDelete.shared) }
    func dismissDelete() { store.dispatch(message: ClientFormMessageDismissDelete.shared) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
