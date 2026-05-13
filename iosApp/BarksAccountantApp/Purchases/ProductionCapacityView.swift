import SwiftUI
import Shared

struct ProductionCapacityView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ProductionCapacityStoreWrapper

    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ProductionCapacityStoreWrapper(
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

    private var recipeIngredients: [ProductIngredient] {
        (store.selectedProduct?.ingredients as? [ProductIngredient]) ?? []
    }

    private var hasRecipe: Bool {
        !recipeIngredients.isEmpty
    }

    var body: some View {
        ZStack {
            screenBackground
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 12) {
                    productStepCard
                    ingredientStepCard
                    availableStepCard
                    if let count = store.productCount {
                        resultCard(count: count)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .navigationTitle("¿Qué puedo producir?")
        }
        .sheet(isPresented: Binding(
            get: { store.showProductPicker },
            set: { if !$0 { store.dismissProductPicker() } }
        )) {
            ProductPickerSheet(
                products: store.availableProducts,
                onSelected: { store.pickProduct($0) }
            )
        }
        .sheet(isPresented: Binding(
            get: { store.showIngredientPicker },
            set: { if !$0 { store.dismissIngredientPicker() } }
        )) {
            recipeIngredientPickerSheet
        }
        .onAppear {
            store.start()
        }
    }

    // MARK: - Steps

    private var productStepCard: some View {
        card(title: "1. Helado") {
            Button {
                store.openProductPicker()
            } label: {
                HStack {
                    Text(store.selectedProduct?.name ?? "Selecciona un helado")
                        .font(.omnes(17, weight: .semiBold))
                        .foregroundStyle(store.selectedProduct == nil ? secondaryText : primaryText)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(secondaryText)
                }
                .padding(.horizontal, 12)
                .frame(height: 48)
                .background(
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .fill(fieldBackground)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .stroke(fieldBorder, lineWidth: 1)
                )
            }
            .buttonStyle(.plain)
        }
    }

    private var ingredientStepCard: some View {
        card(title: "2. Ingrediente") {
            VStack(alignment: .leading, spacing: 6) {
                Button {
                    store.openIngredientPicker()
                } label: {
                    HStack {
                        Text(ingredientButtonText)
                            .font(.omnes(17, weight: .semiBold))
                            .foregroundStyle(store.selectedIngredient == nil ? secondaryText : primaryText)
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(secondaryText)
                    }
                    .padding(.horizontal, 12)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(fieldBackground)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(fieldBorder, lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)
                .disabled(!hasRecipe)
                .opacity(hasRecipe ? 1.0 : 0.6)

                if store.selectedProduct != nil && !hasRecipe {
                    Text("Este helado no tiene receta")
                        .font(.omnes(12))
                        .foregroundStyle(secondaryText)
                }
            }
        }
    }

    private var ingredientButtonText: String {
        if store.selectedProduct == nil {
            return "Elige primero el helado"
        }
        if !hasRecipe {
            return "Sin receta"
        }
        return store.selectedIngredient?.ingredientName ?? "Selecciona un ingrediente"
    }

    private var availableStepCard: some View {
        card(title: "3. Tengo") {
            HStack(spacing: 10) {
                TextField(
                    "0",
                    text: Binding(
                        get: { store.availableQuantityText },
                        set: { store.setAvailable($0) }
                    )
                )
                .keyboardType(.decimalPad)
                .font(.omnes(17, weight: .semiBold).monospacedDigit())
                .foregroundStyle(primaryText)
                .disabled(store.selectedIngredient == nil)
                .padding(.horizontal, 12)
                .frame(height: 48)
                .background(
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .fill(fieldBackground)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .stroke(fieldBorder, lineWidth: 1)
                )

                Text(store.selectedIngredient?.unit.symbol ?? "")
                    .font(.omnes(17, weight: .semiBold))
                    .foregroundStyle(secondaryText)
                    .frame(minWidth: 30, alignment: .leading)
            }
        }
    }

    private func resultCard(count: Double) -> some View {
        card(title: "Resultado") {
            VStack(alignment: .leading, spacing: 10) {
                if count < 1.0 {
                    Text("No alcanza para hacer ni 1 helado")
                        .font(.omnes(16, weight: .semiBold))
                        .foregroundStyle(Color.barksRed)
                } else {
                    Text("Puedes hacer \(formatCapacity(count)) helados de \(store.selectedProduct?.name ?? "")")
                        .font(.omnes(16, weight: .semiBold))
                        .foregroundStyle(primaryText)

                    if !store.others.isEmpty {
                        Text("También necesitas:")
                            .font(.omnes(13))
                            .foregroundStyle(secondaryText)
                            .padding(.top, 4)

                        ForEach(store.others, id: \.ingredientId) { need in
                            HStack {
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
        }
    }

    // MARK: - Ingredient picker sheet (from selected product)

    private var recipeIngredientPickerSheet: some View {
        NavigationStack {
            List {
                ForEach(recipeIngredients, id: \.ingredientId) { ing in
                    Button {
                        store.pickIngredient(ing)
                        store.dismissIngredientPicker()
                    } label: {
                        HStack {
                            Text(ing.ingredientName)
                                .font(.omnes(17))
                                .foregroundStyle(Color.barksPrincipal)
                            Spacer()
                            Text(ing.unit.symbol)
                                .font(.omnes(13, weight: .semiBold))
                                .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(Color.gray.opacity(0.15))
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }
                    }
                }
            }
            .navigationTitle("Ingrediente")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cerrar") { store.dismissIngredientPicker() }
                }
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

    private func formatCapacity(_ c: Double) -> String {
        let floored = floor(c)
        if c == floored {
            return String(format: "%.0f", floored)
        } else {
            let truncated = floor(c * 10) / 10
            return String(format: "%.1f", truncated)
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
