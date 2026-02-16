import Foundation
import Shared

@Observable
final class PurchasesListStoreWrapper {
    private(set) var purchases: [Purchase] = []
    private(set) var isLoading: Bool = true
    private(set) var error: String? = nil

    private let store: PurchasesListStore
    private var collector: FlowCollector<PurchasesListState>?

    init(purchaseRepository: PurchaseRepository) {
        self.store = PurchasesListStore(purchaseRepository: purchaseRepository)
    }

    func start() {
        collector = FlowCollector<PurchasesListState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.purchases = state.purchases as? [Purchase] ?? []
                self.isLoading = state.isLoading
                self.error = state.error
            }
        )
        store.dispatch(message: PurchasesListMessageStarted.shared)
    }

    func reload() {
        store.dispatch(message: PurchasesListMessageStarted.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
