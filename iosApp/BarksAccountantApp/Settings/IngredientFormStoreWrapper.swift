import Foundation
import Shared

@Observable
final class IngredientFormStoreWrapper {
    private(set) var isEditing: Bool = false
    private(set) var name: String = ""
    private(set) var unit: IngredientUnit = .grams
    private(set) var isSaving: Bool = false
    private(set) var savedSuccessfully: Bool = false
    private(set) var showDeleteConfirm: Bool = false
    private(set) var deleteBlockedBy: [String] = []
    private(set) var deletedSuccessfully: Bool = false
    private(set) var canSave: Bool = false
    private(set) var error: String? = nil

    private let store: IngredientFormStore
    private var collector: FlowCollector<IngredientFormState>?

    init(ingredientRepository: IngredientRepository, productRepository: ProductRepository) {
        self.store = IngredientFormStore(
            ingredientRepository: ingredientRepository,
            productRepository: productRepository
        )
    }

    func start(ingredientId: String?) {
        collector = FlowCollector<IngredientFormState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.isEditing = state.isEditing
                self.name = state.name
                self.unit = state.unit
                self.isSaving = state.isSaving
                self.savedSuccessfully = state.savedSuccessfully
                self.showDeleteConfirm = state.showDeleteConfirm
                self.deleteBlockedBy = (state.deleteBlockedBy as? [String]) ?? []
                self.deletedSuccessfully = state.deletedSuccessfully
                self.canSave = state.canSave
                self.error = state.error
            }
        )
        store.dispatch(message: IngredientFormMessageStarted(ingredientId: ingredientId))
    }

    func nameChanged(_ text: String) { store.dispatch(message: IngredientFormMessageNameChanged(text: text)) }
    func unitChanged(_ u: IngredientUnit) { store.dispatch(message: IngredientFormMessageUnitChanged(unit: u)) }
    func saveTapped() { store.dispatch(message: IngredientFormMessageSaveTapped.shared) }
    func deleteTapped() { store.dispatch(message: IngredientFormMessageDeleteTapped.shared) }
    func confirmDelete() { store.dispatch(message: IngredientFormMessageConfirmDelete.shared) }
    func dismissDelete() { store.dispatch(message: IngredientFormMessageDismissDelete.shared) }
    func dismissDeleteBlocked() { store.dispatch(message: IngredientFormMessageDismissDeleteBlocked.shared) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
