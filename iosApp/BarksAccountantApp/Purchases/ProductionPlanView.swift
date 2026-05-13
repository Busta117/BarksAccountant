import SwiftUI
import Shared

struct ProductionPlanView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ProductionPlanStoreWrapper

    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ProductionPlanStoreWrapper(
            productRepository: serviceLocator.productRepository
        ))
    }

    // MARK: - Theme

    private var screenBackground: Color {
        colorScheme == .dark ? .barksBlack : .barksWhite
    }

    private var cardBackground: Color {
        colorScheme == .dark
        ? Color.white.opacity(0.06)
        : Color.barksLightBlue.opacity(0.25)
    }

    private var primaryText: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    private var secondaryText: Color {
        primaryText.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    private var fieldBackground: Color {
        colorScheme == .dark ? Color.white.opacity(0.05) : Color.white.opacity(0.7)
    }

    private var fieldBorder: Color {
        colorScheme == .dark ? Color.white.opacity(0.10) : Color.black.opacity(0.06)
    }

    var body: some View {
        ZStack {
            screenBackground
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 12) {
                    productsCard
                    if !store.needs.isEmpty {
                        needsCard
                    }
                    if !store.productsWithoutRecipe.isEmpty {
                        withoutRecipeCard
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .navigationTitle("Planificar producción")
        }
        .sheet(isPresented: Binding(
            get: { store.showProductPicker },
            set: { if !$0 { store.dismissPicker() } }
        )) {
            ProductPickerSheet(
                products: store.availableProducts.filter { p in
                    !store.rows.contains(where: { $0.productId == p.id })
                },
                onSelected: { store.pickProduct($0) }
            )
        }
        .onAppear {
            store.start()
        }
    }

    // MARK: - Cards

    private var productsCard: some View {
        card(title: "Helados a producir") {
            VStack(alignment: .leading, spacing: 12) {
                if store.rows.isEmpty {
                    Text("Añade helados para ver la materia prima necesaria")
                        .font(.omnes(14))
                        .foregroundStyle(secondaryText)
                } else {
                    ForEach(store.rows, id: \.productId) { row in
                        HStack(spacing: 10) {
                            Text(row.productName)
                                .font(.omnes(15, weight: .semiBold))
                                .foregroundStyle(primaryText)
                                .layoutPriority(1)

                            Spacer(minLength: 8)

                            TextField(
                                "0",
                                text: Binding(
                                    get: { row.quantityText },
                                    set: { store.changeQty(productId: row.productId, text: $0) }
                                )
                            )
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                            .font(.omnes(15, weight: .semiBold).monospacedDigit())
                            .foregroundStyle(primaryText)
                            .frame(width: 60, height: 40)
                            .background(
                                RoundedRectangle(cornerRadius: 10, style: .continuous)
                                    .fill(fieldBackground)
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 10, style: .continuous)
                                    .stroke(fieldBorder, lineWidth: 1)
                            )

                            Button {
                                store.removeRow(productId: row.productId)
                            } label: {
                                Image(systemName: "trash")
                                    .foregroundStyle(Color.barksRed)
                            }
                            .buttonStyle(.borderless)
                        }
                    }
                }

                Button {
                    store.openPicker()
                } label: {
                    Label("Añadir helado", systemImage: "plus")
                        .font(.omnes(15, weight: .semiBold))
                }
                .disabled(store.availableProducts.isEmpty)
                .opacity(store.availableProducts.isEmpty ? 0.5 : 1.0)
            }
        }
    }

    private var needsCard: some View {
        card(title: "Materia prima necesaria") {
            VStack(alignment: .leading, spacing: 10) {
                ForEach(store.needs, id: \.ingredientId) { need in
                    HStack(alignment: .center, spacing: 8) {
                        Text(need.ingredientName)
                            .font(.omnes(15))
                            .foregroundStyle(primaryText)
                        Spacer()
                        Text("\(formatQty(need.displayQuantity)) \(need.displayUnit.symbol)")
                            .font(.omnes(15, weight: .semiBold).monospacedDigit())
                            .foregroundStyle(primaryText)
                    }
                }
            }
        }
    }

    private var withoutRecipeCard: some View {
        card(title: "Productos sin receta") {
            VStack(alignment: .leading, spacing: 6) {
                ForEach(store.productsWithoutRecipe, id: \.self) { name in
                    Text(name)
                        .font(.omnes(15))
                        .foregroundStyle(secondaryText)
                }
                Text("No se incluyen en el cálculo")
                    .font(.omnes(12))
                    .foregroundStyle(secondaryText)
                    .padding(.top, 4)
            }
        }
    }

    // MARK: - Helpers

    private func formatQty(_ q: Double) -> String {
        if q == q.rounded() {
            return String(format: "%.0f", q)
        } else {
            let truncated = floor(q * 100) / 100
            return String(format: "%.2f", truncated)
        }
    }

    // MARK: - Card Container

    private func card(
        title: String?,
        @ViewBuilder content: () -> some View
    ) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            if let title {
                Text(title)
                    .font(.omnes(15, weight: .semiBold))
                    .foregroundStyle(primaryText.opacity(0.85))
            }

            content()
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
