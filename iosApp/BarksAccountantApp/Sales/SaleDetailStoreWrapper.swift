import Foundation
import Shared

@Observable
final class SaleDetailStoreWrapper {
    private(set) var sale: Sale? = nil
    private(set) var isLoading: Bool = true
    private(set) var showPayConfirm: Bool = false
    private(set) var showDeliverConfirm: Bool = false
    private(set) var error: String? = nil
    private(set) var invoiceHtml: String? = nil
    private(set) var isGeneratingInvoice: Bool = false
    private(set) var summaryHtml: String? = nil
    private(set) var isGeneratingSummary: Bool = false

    private let store: SaleDetailStore
    private var collector: FlowCollector<SaleDetailState>?

    init(saleRepository: SaleRepository, clientRepository: ClientRepository, businessInfoRepository: BusinessInfoRepository) {
        self.store = SaleDetailStore(
            saleRepository: saleRepository,
            clientRepository: clientRepository,
            businessInfoRepository: businessInfoRepository
        )
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
                self.invoiceHtml = state.invoiceHtml
                self.isGeneratingInvoice = state.isGeneratingInvoice
                self.summaryHtml = state.summaryHtml
                self.isGeneratingSummary = state.isGeneratingSummary
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

    func exportInvoice() {
        store.dispatch(message: SaleDetailMessageExportTapped.shared)
    }

    func dismissInvoice() {
        store.dispatch(message: SaleDetailMessageInvoiceDismissed.shared)
    }

    func shareSummary() {
        store.dispatch(message: SaleDetailMessageShareSummaryTapped.shared)
    }

    func dismissSummary() {
        store.dispatch(message: SaleDetailMessageSummaryDismissed.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
