import Foundation
import Shared

@Observable
final class StatsStoreWrapper {
    // Filters
    private(set) var availableYears: [Int] = []
    private(set) var selectedYear: Int = 0
    private(set) var selectedMonth: Int? = nil

    // Section 1: Financial Summary
    private(set) var totalSales: Double = 0.0
    private(set) var totalPurchases: Double = 0.0
    private(set) var netProfit: Double = 0.0
    private(set) var marginPercent: Double = 0.0

    // Section 2: Counters
    private(set) var salesCount: Int = 0
    private(set) var averageTicket: Double = 0.0
    private(set) var unpaidTotal: Double = 0.0
    private(set) var undeliveredCount: Int = 0

    // Section 3: Monthly Breakdown
    private(set) var monthlyBreakdown: [MonthlySale] = []

    // Section 4: Top Products
    private(set) var topProducts: [ProductStat] = []

    // Section 5: Top Clients
    private(set) var topClients: [ClientStat] = []

    // Loading
    private(set) var isLoading: Bool = true
    private(set) var error: String? = nil

    private let store: StatsStore
    private var collector: FlowCollector<StatsState>?

    init(saleRepository: SaleRepository, purchaseRepository: PurchaseRepository) {
        self.store = StatsStore(saleRepository: saleRepository, purchaseRepository: purchaseRepository)
    }

    func start() {
        collector = FlowCollector<StatsState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }

                // Filters
                self.availableYears = (state.availableYears as? [KotlinInt])?.map { $0.intValue } ?? []
                self.selectedYear = Int(state.selectedYear)
                self.selectedMonth = state.selectedMonth?.intValue

                // Section 1
                self.totalSales = state.totalSales
                self.totalPurchases = state.totalPurchases
                self.netProfit = state.netProfit
                self.marginPercent = state.marginPercent

                // Section 2
                self.salesCount = Int(state.salesCount)
                self.averageTicket = state.averageTicket
                self.unpaidTotal = state.unpaidTotal
                self.undeliveredCount = Int(state.undeliveredCount)

                // Section 3
                self.monthlyBreakdown = state.monthlyBreakdown as? [MonthlySale] ?? []

                // Section 4
                self.topProducts = state.topProducts as? [ProductStat] ?? []

                // Section 5
                self.topClients = state.topClients as? [ClientStat] ?? []

                // Loading
                self.isLoading = state.isLoading
                self.error = state.error
            }
        )
        store.dispatch(message: StatsMessageStarted.shared)
    }

    func selectYear(_ year: Int) {
        store.dispatch(message: StatsMessageYearSelected(year: Int32(year)))
    }

    func selectMonth(_ month: Int?) {
        if let month {
            store.dispatch(message: StatsMessageMonthSelected(month: KotlinInt(value: Int32(month))))
        } else {
            store.dispatch(message: StatsMessageMonthSelected(month: nil))
        }
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
