import Foundation
import Shared

@Observable
final class IngredientsListStoreWrapper {
    private(set) var ingredients: [Ingredient] = []
    private(set) var isLoading: Bool = true
    private(set) var error: String? = nil

    private let store: IngredientsListStore
    private var collector: FlowCollector<IngredientsListState>?

    init(ingredientRepository: IngredientRepository) {
        self.store = IngredientsListStore(ingredientRepository: ingredientRepository)
    }

    func start() {
        collector = FlowCollector<IngredientsListState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.ingredients = (state.ingredients as? [Ingredient]) ?? []
                self.isLoading = state.isLoading
                self.error = state.error
            }
        )
        store.dispatch(message: IngredientsListMessageStarted.shared)
    }

    func reload() {
        store.dispatch(message: IngredientsListMessageStarted.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
