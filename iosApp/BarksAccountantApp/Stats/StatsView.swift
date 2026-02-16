import SwiftUI
import Shared

private let monthNames = [
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
]

struct StatsView: View {
    let serviceLocator: ServiceLocator
    @State private var store: StatsStoreWrapper

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: StatsStoreWrapper(
            saleRepository: serviceLocator.saleRepository,
            purchaseRepository: serviceLocator.purchaseRepository
        ))
    }

    var body: some View {
        Group {
            if store.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = store.error {
                VStack(spacing: 12) {
                    Text(error).font(.omnes(17)).foregroundStyle(Color.barksPrincipal.opacity(0.6))
                    Button("Reintentar") { store.start() }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                ScrollView {
                    VStack(spacing: 20) {
                        filterSection
                        financialSection
                        countersSection
                        if store.selectedMonth == nil {
                            monthlySection
                        }
                        productsSection
                        clientsSection
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Stats")
        .onAppear { store.start() }
    }

    // MARK: - Filter Section

    private var filterSection: some View {
        VStack(spacing: 12) {
            if !store.availableYears.isEmpty {
                Picker("Año", selection: Binding(
                    get: { store.selectedYear },
                    set: { store.selectYear($0) }
                )) {
                    ForEach(store.availableYears, id: \.self) { year in
                        Text(String(year)).tag(year)
                    }
                }
                .pickerStyle(.segmented)
            }

            Picker("Mes", selection: Binding(
                get: { store.selectedMonth ?? 0 },
                set: { store.selectMonth($0 == 0 ? nil : $0) }
            )) {
                Text("Todos").tag(0)
                ForEach(1...12, id: \.self) { month in
                    Text(monthNames[month - 1]).tag(month)
                }
            }
            .pickerStyle(.menu)
        }
    }

    // MARK: - Section 1: Financial Summary

    private var financialSection: some View {
        SectionCard(title: "Resumen") {
            if store.salesCount == 0 && store.totalPurchases == 0 {
                emptyMessage
            } else {
                VStack(spacing: 8) {
                    StatRow(label: "Ventas totales", value: formatCurrency(store.totalSales))
                    StatRow(label: "Compras totales", value: formatCurrency(store.totalPurchases))
                    StatRow(
                        label: "Ganancia neta",
                        value: formatCurrency(store.netProfit),
                        valueColor: store.netProfit >= 0 ? .green : Color.barksRed
                    )
                    StatRow(label: "Margen", value: String(format: "%.1f%%", store.marginPercent))
                }
            }
        }
    }

    // MARK: - Section 2: Counters

    private var countersSection: some View {
        SectionCard(title: "Indicadores") {
            if store.salesCount == 0 {
                emptyMessage
            } else {
                VStack(spacing: 8) {
                    StatRow(label: "Cantidad de ventas", value: "\(store.salesCount)")
                    StatRow(label: "Ticket promedio", value: formatCurrency(store.averageTicket))
                    StatRow(label: "Pendiente de pago", value: formatCurrency(store.unpaidTotal))
                    StatRow(label: "Sin entregar", value: "\(store.undeliveredCount)")
                }
            }
        }
    }

    // MARK: - Section 3: Monthly Breakdown

    private var monthlySection: some View {
        SectionCard(title: "Desglose mensual") {
            if store.monthlyBreakdown.isEmpty {
                emptyMessage
            } else {
                VStack(spacing: 8) {
                    ForEach(store.monthlyBreakdown, id: \.month) { item in
                        StatRow(
                            label: monthNames[Int(item.month) - 1],
                            value: formatCurrency(item.total)
                        )
                    }
                }
            }
        }
    }

    // MARK: - Section 4: Top Products

    private var productsSection: some View {
        SectionCard(title: "Productos más vendidos") {
            if store.topProducts.isEmpty {
                emptyMessage
            } else {
                VStack(spacing: 10) {
                    ForEach(store.topProducts, id: \.name) { product in
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(product.name).font(.omnes(17))
                                Text("\(product.unitsSold) uds")
                                    .font(.omnes(12))
                                    .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                            }
                            Spacer()
                            Text(formatCurrency(product.revenue))
                                .font(.omnes(17, weight: .medium))
                        }
                    }
                }
            }
        }
    }

    // MARK: - Section 5: Top Clients

    private var clientsSection: some View {
        SectionCard(title: "Principales clientes") {
            if store.topClients.isEmpty {
                emptyMessage
            } else {
                VStack(spacing: 10) {
                    ForEach(store.topClients, id: \.name) { client in
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(client.name).font(.omnes(17))
                                Text("\(client.orderCount) pedido\(client.orderCount == 1 ? "" : "s")")
                                    .font(.omnes(12))
                                    .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                            }
                            Spacer()
                            Text(formatCurrency(client.totalAmount))
                                .font(.omnes(17, weight: .medium))
                        }
                    }
                }
            }
        }
    }

    // MARK: - Helpers

    private var emptyMessage: some View {
        Text("Sin datos para mostrar")
            .font(.omnes(15))
            .foregroundStyle(Color.barksPrincipal.opacity(0.6))
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
    }

    private func formatCurrency(_ value: Double) -> String {
        String(format: "€%.2f", value)
    }
}

// MARK: - Reusable Components

private struct SectionCard<Content: View>: View {
    let title: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.omnes(17, weight: .semiBold))
                .foregroundStyle(Color.barksPrincipal)
            content
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct StatRow: View {
    let label: String
    let value: String
    var valueColor: Color = Color.barksPrincipal

    var body: some View {
        HStack {
            Text(label)
                .font(.omnes(15))
                .foregroundStyle(Color.barksPrincipal.opacity(0.6))
            Spacer()
            Text(value)
                .font(.omnes(15, weight: .semiBold))
                .foregroundStyle(valueColor)
        }
    }
}
