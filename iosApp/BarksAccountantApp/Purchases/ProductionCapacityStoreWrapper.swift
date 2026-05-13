import Foundation
import Shared

@Observable
final class ProductionCapacityStoreWrapper {
    private(set) var availableProducts: [Product] = []
    private(set) var selectedProduct: Product? = nil
    private(set) var selectedIngredient: ProductIngredient? = nil
    private(set) var availableQuantityText: String = ""
    private(set) var showProductPicker: Bool = false
    private(set) var showIngredientPicker: Bool = false
    private(set) var productCount: Double? = nil
    private(set) var others: [RawMaterialNeed] = []

    private let store: ProductionCapacityStore
    private var collector: FlowCollector<ProductionCapacityState>?

    init(productRepository: ProductRepository) {
        self.store = ProductionCapacityStore(productRepository: productRepository)
    }

    func start() {
        collector = FlowCollector<ProductionCapacityState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.availableProducts = (state.availableProducts as? [Product]) ?? []
                self.selectedProduct = state.selectedProduct
                self.selectedIngredient = state.selectedIngredient
                self.availableQuantityText = state.availableQuantityText
                self.showProductPicker = state.showProductPicker
                self.showIngredientPicker = state.showIngredientPicker
                if let result = state.result {
                    self.productCount = result.productCount
                    self.others = (result.otherIngredientsNeeded as? [RawMaterialNeed]) ?? []
                } else {
                    self.productCount = nil
                    self.others = []
                }
            }
        )
        store.dispatch(message: ProductionCapacityMessageStarted.shared)
    }

    func openProductPicker() { store.dispatch(message: ProductionCapacityMessageProductPickerOpened.shared) }
    func dismissProductPicker() { store.dispatch(message: ProductionCapacityMessageDismissProductPicker.shared) }
    func pickProduct(_ p: Product) { store.dispatch(message: ProductionCapacityMessageProductPicked(product: p)) }
    func openIngredientPicker() { store.dispatch(message: ProductionCapacityMessageIngredientPickerOpened.shared) }
    func dismissIngredientPicker() { store.dispatch(message: ProductionCapacityMessageDismissIngredientPicker.shared) }
    func pickIngredient(_ i: ProductIngredient) { store.dispatch(message: ProductionCapacityMessageIngredientPicked(ingredient: i)) }
    func setAvailable(_ t: String) { store.dispatch(message: ProductionCapacityMessageAvailableQuantityChanged(text: t)) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
