import Foundation
import Shared

@Observable
final class ProductFormStoreWrapper {
    private(set) var isEditing: Bool = false
    private(set) var name: String = ""
    private(set) var price: String = ""
    private(set) var productIngredients: [ProductIngredient] = []
    private(set) var ingredientQuantityTexts: [String] = []
    private(set) var availableIngredients: [Ingredient] = []
    private(set) var showIngredientPicker: Bool = false
    private(set) var isSaving: Bool = false
    private(set) var savedSuccessfully: Bool = false
    private(set) var showDeleteConfirm: Bool = false
    private(set) var deletedSuccessfully: Bool = false
    private(set) var error: String? = nil
    private(set) var canSave: Bool = false

    private let store: ProductFormStore
    private var collector: FlowCollector<ProductFormState>?

    init(productRepository: ProductRepository, ingredientRepository: IngredientRepository) {
        self.store = ProductFormStore(
            productRepository: productRepository,
            ingredientRepository: ingredientRepository
        )
    }

    func start(productId: String?) {
        collector = FlowCollector<ProductFormState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.isEditing = state.isEditing
                self.name = state.name
                self.price = state.price
                self.productIngredients = (state.ingredients as? [ProductIngredient]) ?? []
                self.ingredientQuantityTexts = (state.ingredientQuantityTexts as? [String]) ?? []
                self.availableIngredients = (state.availableIngredients as? [Ingredient]) ?? []
                self.showIngredientPicker = state.showIngredientPicker
                self.isSaving = state.isSaving
                self.savedSuccessfully = state.savedSuccessfully
                self.showDeleteConfirm = state.showDeleteConfirm
                self.deletedSuccessfully = state.deletedSuccessfully
                self.error = state.error
                self.canSave = state.canSave
            }
        )
        store.dispatch(message: ProductFormMessageStarted(productId: productId))
    }

    func nameChanged(_ text: String) { store.dispatch(message: ProductFormMessageNameChanged(text: text)) }
    func priceChanged(_ text: String) { store.dispatch(message: ProductFormMessagePriceChanged(text: text)) }
    func addIngredientTapped() { store.dispatch(message: ProductFormMessageAddIngredientTapped.shared) }
    func dismissIngredientPicker() { store.dispatch(message: ProductFormMessageDismissIngredientPicker.shared) }
    func ingredientPicked(_ i: Ingredient) { store.dispatch(message: ProductFormMessageIngredientPicked(ingredient: i)) }
    func ingredientQuantityChanged(index: Int, text: String) {
        store.dispatch(message: ProductFormMessageIngredientQuantityChanged(index: Int32(index), text: text))
    }
    func ingredientRemoved(index: Int) {
        store.dispatch(message: ProductFormMessageIngredientRemoved(index: Int32(index)))
    }
    func saveTapped() { store.dispatch(message: ProductFormMessageSaveTapped.shared) }
    func deleteTapped() { store.dispatch(message: ProductFormMessageDeleteTapped.shared) }
    func confirmDelete() { store.dispatch(message: ProductFormMessageConfirmDelete.shared) }
    func dismissDelete() { store.dispatch(message: ProductFormMessageDismissDelete.shared) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
