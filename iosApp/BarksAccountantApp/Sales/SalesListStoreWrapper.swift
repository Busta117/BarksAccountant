import Foundation
import Shared

@Observable
final class SalesListStoreWrapper {
    private(set) var sales: [Sale] = []
    private(set) var isLoading: Bool = true
    private(set) var error: String? = nil

    private let store: SalesListStore
    private var collector: FlowCollector<SalesListState>?

    init(saleRepository: SaleRepository) {
        self.store = SalesListStore(saleRepository: saleRepository)
    }

    func start() {
        collector = FlowCollector<SalesListState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.sales = state.sales
                self.isLoading = state.isLoading
                self.error = state.error
            }
        )
        store.dispatch(message: SalesListMessageStarted.shared)
    }

    func reload() {
        store.dispatch(message: SalesListMessageStarted.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}

