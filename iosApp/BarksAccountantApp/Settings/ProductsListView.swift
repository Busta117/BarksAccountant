import SwiftUI
import Shared

struct ProductsListView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ProductsListStoreWrapper

    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ProductsListStoreWrapper(
            productRepository: serviceLocator.productRepository
        ))
    }

    // MARK: - Theme

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
                .navigationTitle("Productos")
                .onAppear { store.start() }

            fabAddButton
        }
        .navigationDestination(for: ProductDestination.self) { destination in
            switch destination {
            case .new:
                ProductFormView(
                    serviceLocator: serviceLocator,
                    productId: nil,
                    onSaved: { store.reload() }
                )
            case .edit(let productId):
                ProductFormView(
                    serviceLocator: serviceLocator,
                    productId: productId,
                    onSaved: { store.reload() }
                )
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        Group {
            if store.isLoading && store.products.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if let error = store.error, store.products.isEmpty {
                VStack(spacing: 12) {
                    Text(error)
                        .font(.omnes(17))
                        .foregroundStyle(primaryText.opacity(0.7))

                    Button("Reintentar") { store.reload() }
                        .buttonStyle(.borderedProminent)
                        .tint(.barksRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.horizontal, 24)

            } else if store.products.isEmpty {
                Text("No hay productos")
                    .font(.vagRundschrift(20))
                    .foregroundStyle(primaryText.opacity(0.7))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else {
                List {
                    ForEach(store.products, id: \.id) { product in
                        NavigationLink(value: ProductDestination.edit(product.id)) {
                            ProductCardRow(
                                name: product.name,
                                price: product.unitPrice,
                                cardBackground: cardBackground
                            )
                        }
                        .buttonStyle(.plain)
                        .listRowSeparator(.hidden)
                        .listRowInsets(.init(top: 8, leading: 16, bottom: 8, trailing: 16))
                        .listRowBackground(Color.clear)
                    }
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
    }

    /// Floating action button for creating a new product
    private var fabAddButton: some View {
        NavigationLink(value: ProductDestination.new) {
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
                .accessibilityLabel("Add product")
        }
        .padding(.trailing, 20)
        .padding(.bottom, 20)
    }
}

private struct ProductCardRow: View {
    let name: String
    let price: Double
    let cardBackground: Color

    @Environment(\.colorScheme) private var colorScheme

    /// Title color adapts to appearance mode
    private var titleColor: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    /// Price uses a single solid color (consistent with the rest of the app)
    private var priceColor: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    /// Secondary text color for subtle info if needed later
    private var secondaryColor: Color {
        titleColor.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Text(name)
                .font(.omnes(17, weight: .semiBold))
                .foregroundStyle(titleColor)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)
                .layoutPriority(1)

            Spacer(minLength: 12)

            Text(String(format: "€%.2f", price))
                .font(.omnes(17, weight: .semiBold).monospacedDigit())
                .foregroundStyle(priceColor)
                .layoutPriority(0)
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
