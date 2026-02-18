import SwiftUI
import Shared

struct SalesListView: View {
    let serviceLocator: ServiceLocator
    let personName: String
    @State private var store: SalesListStoreWrapper

    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator, personName: String) {
        self.serviceLocator = serviceLocator
        self.personName = personName
        _store = State(initialValue: SalesListStoreWrapper(
            saleRepository: serviceLocator.saleRepository
        ))
    }

    /// Screen background depending on appearance mode
    private var screenBackground: Color {
        colorScheme == .dark ? .barksBlack : .barksWhite
    }

    /// Card background used for all rows (same style for every cell)
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

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            screenBackground
                .ignoresSafeArea()

            content
                .navigationTitle("Ventas")
                .onAppear { store.start() }

            fabAddButton
        }
    }

    @ViewBuilder
    private var content: some View {
        Group {
            if store.isLoading && store.sales.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if let error = store.error, store.sales.isEmpty {
                VStack(spacing: 12) {
                    Text(error)
                        .font(.omnes(17))
                        .foregroundStyle(primaryText.opacity(0.7))

                    Button("Reintentar") {
                        store.reload()
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.barksRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.horizontal, 24)

            } else if store.sales.isEmpty {
                Text("No hay ventas")
                    .font(.vagRundschrift(20))
                    .foregroundStyle(primaryText.opacity(0.7))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else {
                List {
                    ForEach(store.sales, id: \.id) { sale in
                        NavigationLink(value: sale.id) {
                            SaleCardRow(
                                sale: sale,
                                cardBackground: cardBackground
                            )
                        }
                        // Remove default separator
                        .listRowSeparator(.hidden)
                        // Add spacing around each card
                        .listRowInsets(.init(
                            top: 8,
                            leading: 16,
                            bottom: 8,
                            trailing: 16
                        ))
                        .listRowBackground(Color.clear)
                    }
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
        // Navigation handling for editing or creating a sale
        .navigationDestination(for: String.self) { value in
            if value == "new_sale" {
                SaleFormView(
                    serviceLocator: serviceLocator,
                    saleId: nil,
                    personName: personName,
                    onSaved: { store.reload() }
                )
            } else {
                SaleDetailView(
                    serviceLocator: serviceLocator,
                    saleId: value,
                    personName: personName,
                    onSaleUpdated: { store.reload() }
                )
            }
        }
    }

    /// Floating action button for creating a new sale
    private var fabAddButton: some View {
        NavigationLink(value: "new_sale") {
            Image(systemName: "plus")
                .font(.system(size: 20, weight: .bold))
                .foregroundStyle(Color.barksWhite)
                .frame(width: 56, height: 56)
                .background(Color.barksRed)
                .clipShape(Circle())
                .shadow(
                    color: .black.opacity(colorScheme == .dark ? 0.35 : 0.18),
                    radius: 10,
                    x: 0,
                    y: 6
                )
                .accessibilityLabel("Add sale")
        }
        .padding(.trailing, 20)
        .padding(.bottom, 20)
    }
}

struct SaleCardRow: View {
    let sale: Sale
    let cardBackground: Color

    @Environment(\.colorScheme) private var colorScheme

    /// Title color adapts to appearance mode
    private var titleColor: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    /// Secondary text color for date and status
    private var secondaryColor: Color {
        titleColor.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    /// Price uses a single solid color (requirement)
    private var priceColor: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Unpaid indicator
            if !sale.isPaid {
                RoundedRectangle(cornerRadius: 2, style: .continuous)
                    .fill(Color.barksRed)
                    .frame(width: 4)
            }

            VStack(alignment: .leading, spacing: 6) {
                Text(sale.clientName)
                    .font(.omnes(17, weight: .semiBold))
                    .foregroundStyle(titleColor)
                    .lineLimit(1)

                HStack(spacing: 8) {
                    Image(systemName: "calendar")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(Color.barksLightBlue)

                    // Keep original string (no date formatting change)
                    Text(sale.orderDate)
                        .font(.omnes(15))
                        .foregroundStyle(secondaryColor)
                        .lineLimit(1)
                }

                Text(sale.isPaid ? "Pagada" : "Pendiente")
                    .font(.omnes(13, weight: .semiBold))
                    .foregroundStyle(
                        sale.isPaid
                        ? secondaryColor
                        : Color.barksRed.opacity(colorScheme == .dark ? 0.9 : 1.0)
                    )
            }

            Spacer(minLength: 12)

            Text(String(format: "€%.2f", sale.totalPrice))
                .font(.omnes(17, weight: .semiBold))
                .foregroundStyle(priceColor)
        }
        .padding(.vertical, 14)
        .padding(.horizontal, 16)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(cardBackground)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(Color.white.opacity(colorScheme == .dark ? 0.06 : 0.0), lineWidth: 1)
        )
        .shadow(
            color: .black.opacity(colorScheme == .dark ? 0.22 : 0.08),
            radius: 16,
            x: 0,
            y: 10
        )
    }
}
