import Foundation
import Shared

@Observable
final class ProductsListStoreWrapper {
    private(set) var products: [Product] = []
    private(set) var isLoading: Bool = true
    private(set) var error: String? = nil

    private let store: ProductsListStore
    private var collector: FlowCollector<ProductsListState>?

    init(productRepository: ProductRepository) {
        self.store = ProductsListStore(productRepository: productRepository)
    }

    func start() {
        collector = FlowCollector<ProductsListState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.products = state.products as? [Product] ?? []
                self.isLoading = state.isLoading
                self.error = state.error
            }
        )
        store.dispatch(message: ProductsListMessageStarted.shared)
    }

    func reload() {
        store.dispatch(message: ProductsListMessageStarted.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
