import Foundation
import Shared

@Observable
final class ProductionPlanStoreWrapper {
    private(set) var availableProducts: [Product] = []
    private(set) var rows: [PlanRow] = []
    private(set) var showProductPicker: Bool = false
    private(set) var isLoading: Bool = false
    private(set) var needs: [RawMaterialNeed] = []
    private(set) var productsWithoutRecipe: [String] = []
    private(set) var error: String? = nil

    private let store: ProductionPlanStore
    private var collector: FlowCollector<ProductionPlanState>?

    init(productRepository: ProductRepository) {
        self.store = ProductionPlanStore(productRepository: productRepository)
    }

    func start() {
        collector = FlowCollector<ProductionPlanState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.availableProducts = (state.availableProducts as? [Product]) ?? []
                self.rows = (state.rows as? [PlanRow]) ?? []
                self.showProductPicker = state.showProductPicker
                self.isLoading = state.isLoading
                let r = state.result
                self.needs = (r.needs as? [RawMaterialNeed]) ?? []
                self.productsWithoutRecipe = (r.productsWithoutRecipe as? [String]) ?? []
                self.error = state.error
            }
        )
        store.dispatch(message: ProductionPlanMessageStarted.shared)
    }

    func openPicker() { store.dispatch(message: ProductionPlanMessageAddProductTapped.shared) }
    func dismissPicker() { store.dispatch(message: ProductionPlanMessageDismissPicker.shared) }
    func pickProduct(_ p: Product) { store.dispatch(message: ProductionPlanMessageProductPicked(product: p)) }
    func changeQty(productId: String, text: String) {
        store.dispatch(message: ProductionPlanMessageQuantityChanged(productId: productId, text: text))
    }
    func removeRow(productId: String) {
        store.dispatch(message: ProductionPlanMessageRowRemoved(productId: productId))
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
