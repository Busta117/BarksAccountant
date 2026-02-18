import SwiftUI
import Shared

private let monthNames = [
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
]

struct StatsView: View {
    let serviceLocator: ServiceLocator
    @State private var store: StatsStoreWrapper

    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: StatsStoreWrapper(
            saleRepository: serviceLocator.saleRepository,
            purchaseRepository: serviceLocator.purchaseRepository
        ))
    }

    // MARK: - Theme

    /// Screen background depending on appearance mode
    private var screenBackground: Color {
        colorScheme == .dark ? .barksBlack : .barksWhite
    }

    /// Card background used across the entire screen
    private var cardBackground: Color {
        if colorScheme == .dark {
            return Color.white.opacity(0.06)
        } else {
            return Color.barksLightBlue.opacity(0.25)
        }
    }

    /// Primary text color depending on appearance mode
    private var primaryText: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    /// Secondary text color depending on appearance mode
    private var secondaryText: Color {
        primaryText.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    var body: some View {
        ZStack {
            screenBackground
                .ignoresSafeArea()

            Group {
                if store.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)

                } else if let error = store.error {
                    VStack(spacing: 12) {
                        Text(error)
                            .font(.omnes(17))
                            .foregroundStyle(secondaryText)

                        Button("Reintentar") { store.start() }
                            .buttonStyle(.borderedProminent)
                            .tint(.barksRed)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .padding(.horizontal, 24)

                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            filterSection
                            financialSection
                            countersSection

                            if store.selectedMonth == nil {
                                monthlySection
                            }

                            productsSection
                            clientsSection
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                    }
                }
            }
        }
        .navigationTitle("Stats")
        .onAppear { store.start() }
    }

    // MARK: - Filter Section

    private var filterSection: some View {
        SectionCard(
            title: "Filtro",
            cardBackground: cardBackground,
            primaryText: primaryText,
            secondaryText: secondaryText,
            colorScheme: colorScheme
        ) {
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
                    .tint(.barksRed)
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
                .tint(.barksRed)
            }
        }
    }

    // MARK: - Section 1: Financial Summary

    private var financialSection: some View {
        SectionCard(
            title: "Resumen",
            cardBackground: cardBackground,
            primaryText: primaryText,
            secondaryText: secondaryText,
            colorScheme: colorScheme
        ) {
            if store.salesCount == 0 && store.totalPurchases == 0 {
                emptyMessage
            } else {
                VStack(spacing: 8) {
                    StatRow(label: "Ventas totales", value: formatCurrency(store.totalSales), secondaryText: secondaryText, valueColor: primaryText)
                    StatRow(label: "Compras totales", value: formatCurrency(store.totalPurchases), secondaryText: secondaryText, valueColor: primaryText)
                    StatRow(
                        label: "Ganancia neta",
                        value: formatCurrency(store.netProfit),
                        secondaryText: secondaryText,
                        valueColor: store.netProfit >= 0 ? .green : .barksRed
                    )
                    StatRow(label: "Margen", value: String(format: "%.1f%%", store.marginPercent), secondaryText: secondaryText, valueColor: primaryText)
                }
            }
        }
    }

    // MARK: - Section 2: Counters

    private var countersSection: some View {
        SectionCard(
            title: "Indicadores",
            cardBackground: cardBackground,
            primaryText: primaryText,
            secondaryText: secondaryText,
            colorScheme: colorScheme
        ) {
            if store.salesCount == 0 {
                emptyMessage
            } else {
                VStack(spacing: 8) {
                    StatRow(label: "Cantidad de ventas", value: "\(store.salesCount)", secondaryText: secondaryText, valueColor: primaryText)
                    StatRow(label: "Ticket promedio", value: formatCurrency(store.averageTicket), secondaryText: secondaryText, valueColor: primaryText)
                    StatRow(label: "Pendiente de pago", value: formatCurrency(store.unpaidTotal), secondaryText: secondaryText, valueColor: primaryText)
                    StatRow(label: "Sin entregar", value: "\(store.undeliveredCount)", secondaryText: secondaryText, valueColor: primaryText)
                }
            }
        }
    }

    // MARK: - Section 3: Monthly Breakdown

    private var monthlySection: some View {
        SectionCard(
            title: "Desglose mensual",
            cardBackground: cardBackground,
            primaryText: primaryText,
            secondaryText: secondaryText,
            colorScheme: colorScheme
        ) {
            if store.monthlyBreakdown.isEmpty {
                emptyMessage
            } else {
                VStack(spacing: 8) {
                    ForEach(store.monthlyBreakdown, id: \.month) { item in
                        StatRow(
                            label: monthNames[Int(item.month) - 1],
                            value: formatCurrency(item.total),
                            secondaryText: secondaryText,
                            valueColor: primaryText
                        )
                    }
                }
            }
        }
    }

    // MARK: - Section 4: Top Products

    private var productsSection: some View {
        SectionCard(
            title: "Productos más vendidos",
            cardBackground: cardBackground,
            primaryText: primaryText,
            secondaryText: secondaryText,
            colorScheme: colorScheme
        ) {
            if store.topProducts.isEmpty {
                emptyMessage
            } else {
                VStack(spacing: 10) {
                    ForEach(store.topProducts, id: \.name) { product in
                        HStack(alignment: .top, spacing: 12) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(product.name)
                                    .font(.omnes(17, weight: .semiBold))
                                    .foregroundStyle(primaryText)
                                    .lineLimit(2)
                                    .fixedSize(horizontal: false, vertical: true)

                                Text("\(product.unitsSold) uds")
                                    .font(.omnes(12))
                                    .foregroundStyle(secondaryText)
                            }

                            Spacer(minLength: 12)

                            Text(formatCurrency(product.revenue))
                                .font(.omnes(17, weight: .semiBold))
                                .foregroundStyle(primaryText)
                        }
                    }
                }
            }
        }
    }

    // MARK: - Section 5: Top Clients

    private var clientsSection: some View {
        SectionCard(
            title: "Principales clientes",
            cardBackground: cardBackground,
            primaryText: primaryText,
            secondaryText: secondaryText,
            colorScheme: colorScheme
        ) {
            if store.topClients.isEmpty {
                emptyMessage
            } else {
                VStack(spacing: 10) {
                    ForEach(store.topClients, id: \.name) { client in
                        HStack(alignment: .top, spacing: 12) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(client.name)
                                    .font(.omnes(17, weight: .semiBold))
                                    .foregroundStyle(primaryText)
                                    .lineLimit(2)
                                    .fixedSize(horizontal: false, vertical: true)

                                Text("\(client.orderCount) pedido\(client.orderCount == 1 ? "" : "s")")
                                    .font(.omnes(12))
                                    .foregroundStyle(secondaryText)
                            }

                            Spacer(minLength: 12)

                            Text(formatCurrency(client.totalAmount))
                                .font(.omnes(17, weight: .semiBold))
                                .foregroundStyle(primaryText)
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
            .foregroundStyle(secondaryText)
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
    let cardBackground: Color
    let primaryText: Color
    let secondaryText: Color
    let colorScheme: ColorScheme
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.omnes(15, weight: .semiBold))
                .foregroundStyle(primaryText.opacity(0.85))

            content
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(cardBackground)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(Color.white.opacity(colorScheme == .dark ? 0.06 : 0.0), lineWidth: 1)
        )
        .shadow(
            color: .black.opacity(colorScheme == .dark ? 0.18 : 0.08),
            radius: 16,
            x: 0,
            y: 10
        )
    }
}

private struct StatRow: View {
    let label: String
    let value: String
    let secondaryText: Color
    let valueColor: Color

    var body: some View {
        HStack(spacing: 12) {
            Text(label)
                .font(.omnes(15))
                .foregroundStyle(secondaryText)
                .lineLimit(1)

            Spacer(minLength: 12)

            Text(value)
                .font(.omnes(15, weight: .semiBold).monospacedDigit())
                .foregroundStyle(valueColor)
        }
    }
}
