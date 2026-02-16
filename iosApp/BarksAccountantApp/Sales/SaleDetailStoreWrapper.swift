import Foundation
import Shared

@Observable
final class SaleDetailStoreWrapper {
    private(set) var sale: Sale? = nil
    private(set) var isLoading: Bool = true
    private(set) var showPayConfirm: Bool = false
    private(set) var showDeliverConfirm: Bool = false
    private(set) var error: String? = nil

    private let store: SaleDetailStore
    private var collector: FlowCollector<SaleDetailState>?

    init(saleRepository: SaleRepository) {
        self.store = SaleDetailStore(saleRepository: saleRepository)
    }

    func start(saleId: String) {
        collector = FlowCollector<SaleDetailState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.sale = state.sale
                self.isLoading = state.isLoading
                self.showPayConfirm = state.showPayConfirm
                self.showDeliverConfirm = state.showDeliverConfirm
                self.error = state.error
            }
        )
        store.dispatch(message: SaleDetailMessageStarted(saleId: saleId))
    }

    func markAsPaidTapped() {
        store.dispatch(message: SaleDetailMessageMarkAsPaidTapped.shared)
    }

    func confirmPaid() {
        store.dispatch(message: SaleDetailMessageConfirmPaid.shared)
    }

    func markAsDeliveredTapped() {
        store.dispatch(message: SaleDetailMessageMarkAsDeliveredTapped.shared)
    }

    func confirmDelivered() {
        store.dispatch(message: SaleDetailMessageConfirmDelivered.shared)
    }

    func dismissConfirm() {
        store.dispatch(message: SaleDetailMessageDismissConfirm.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
