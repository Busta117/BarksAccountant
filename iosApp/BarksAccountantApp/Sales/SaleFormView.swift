import SwiftUI
import Shared

struct SaleFormView: View {
    let serviceLocator: ServiceLocator
    let saleId: String?
    var onSaved: () -> Void

    @State private var store: SaleFormStoreWrapper
    @State private var showClientPicker = false
    @State private var showProductPicker = false

    @State private var orderDateValue = Date()
    @State private var deliveryDateValue = Date()
    @State private var hasDeliveryDate = false

    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator, saleId: String?, personName: String, onSaved: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.saleId = saleId
        self.onSaved = onSaved
        _store = State(initialValue: SaleFormStoreWrapper(
            saleRepository: serviceLocator.saleRepository,
            productRepository: serviceLocator.productRepository,
            clientRepository: serviceLocator.clientRepository,
            personName: personName
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

    // MARK: - Body

    var body: some View {
        ZStack {
            screenBackground.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 12) {
                    clientCard
                    datesCard
                    productsCard
                    totalCard
                    saveCard
                    deleteCard
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .navigationTitle(store.isEditing ? "Editar Venta" : "Nueva Venta")
        }
        .sheet(isPresented: $showClientPicker) {
            ClientPickerSheet(clients: store.clients) { name in
                store.clientSelected(name)
            }
        }
        .sheet(isPresented: $showProductPicker) {
            ProductPickerSheet(products: store.availableProducts) { product in
                store.addProduct(product)
            }
        }
        .alert("Eliminar Venta", isPresented: Binding(
            get: { store.showDeleteConfirm },
            set: { if !$0 { store.dismissDelete() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        }
        .onAppear {
            store.start(saleId: saleId)
            store.orderDateChanged(formatDate(orderDateValue))
            store.deliveryDateChanged(nil)
        }
        .onChange(of: store.savedSuccessfully) { _, saved in
            if saved {
                onSaved()
                dismiss()
            }
        }
        .onChange(of: store.deletedSuccessfully) { _, deleted in
            if deleted {
                onSaved()
                dismiss()
            }
        }
    }

    // MARK: - Cards

    private var clientCard: some View {
        card(title: "Cliente") {
            Button(action: { showClientPicker = true }) {
                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(store.clientName.isEmpty ? "Seleccionar cliente" : store.clientName)
                            .font(.omnes(17, weight: .semiBold))
                            .foregroundStyle(store.clientName.isEmpty ? secondaryText : primaryText)
                        
                        if store.clientName.isEmpty {
                            Text("Requerido")
                                .font(.omnes(13))
                                .foregroundStyle(Color.barksRed.opacity(colorScheme == .dark ? 0.9 : 1.0))
                        }
                    }
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(secondaryText)
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
    }

    private var datesCard: some View {
        card(title: "Fechas") {
            VStack(spacing: 12) {
                DatePicker(
                    "Fecha de pedido",
                    selection: $orderDateValue,
                    displayedComponents: .date
                )
                .tint(.barksRed)
                .onChange(of: orderDateValue) { _, newDate in
                    store.orderDateChanged(formatDate(newDate))
                }

                Toggle("Fecha de entrega", isOn: $hasDeliveryDate)
                    .tint(.barksRed)
                    .onChange(of: hasDeliveryDate) { _, has in
                        has
                        ? store.deliveryDateChanged(formatDate(deliveryDateValue))
                        : store.deliveryDateChanged(nil)
                    }

                if hasDeliveryDate {
                    DatePicker(
                        "Entrega",
                        selection: $deliveryDateValue,
                        displayedComponents: .date
                    )
                    .tint(.barksRed)
                    .onChange(of: deliveryDateValue) { _, newDate in
                        store.deliveryDateChanged(formatDate(newDate))
                    }
                }
            }
        }
    }

    private var productsCard: some View {
        card(title: "Productos") {
            VStack(spacing: 12) {

                if store.products.isEmpty {
                    Text("Agrega al menos un producto")
                        .foregroundStyle(secondaryText)
                        .frame(maxWidth: .infinity, alignment: .leading)
                } else {
                    VStack(spacing: 12) {
                        ForEach(Array(store.products.enumerated()), id: \.offset) { index, product in
                            productRow(index: index, product: product)
                            if index != store.products.indices.last {
                                Divider().opacity(0.2)
                            }
                        }
                    }
                }

                Button(action: { showProductPicker = true }) {
                    HStack {
                        Image(systemName: "plus.circle.fill")
                            .foregroundStyle(Color.barksRed)
                        Text("Agregar producto")
                            .font(.omnes(16, weight: .semiBold))
                        Spacer()
                    }
                    .frame(height: 44)
                }
                .buttonStyle(.plain)
            }
        }
    }

    // MARK: - Product Row (FIXED)

    private func productRow(index: Int, product: SaleProduct) -> some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(product.name)
                    .font(.omnes(17, weight: .semiBold))
                    .foregroundStyle(primaryText)
                    .lineLimit(2)
                    .fixedSize(horizontal: false, vertical: true)
                    .layoutPriority(1)

                Text(String(format: "€%.2f", product.unitPrice))
                    .font(.omnes(13))
                    .foregroundStyle(secondaryText)
            }

            Spacer(minLength: 8)

            rightControls(product: product, index: index)
        }
    }

    private func rightControls(product: SaleProduct, index: Int) -> some View {
        HStack(spacing: 12) {
            Button {
                store.decrementQuantity(at: index)
            } label: {
                Image(systemName: product.quantity <= 1 ? "trash" : "minus.circle")
                    .foregroundStyle(product.quantity <= 1 ? Color.barksRed : Color.barksLightBlue)
            }

            Text("\(product.quantity)")
                .font(.omnes(17, weight: .semiBold).monospacedDigit())
                .frame(minWidth: 28)

            Button {
                store.incrementQuantity(at: index)
            } label: {
                Image(systemName: "plus.circle")
                    .foregroundStyle(Color.barksLightBlue)
            }

            Text(String(format: "€%.2f", product.totalPrice))
                .font(.omnes(17, weight: .semiBold).monospacedDigit())
                .frame(width: 84, alignment: .trailing)
        }
    }

    private var totalCard: some View {
        card(title: nil) {
            HStack {
                Text("Total")
                    .font(.omnes(18, weight: .bold))

                Spacer()

                Text(String(format: "€%.2f", store.totalPrice))
                    .font(.omnes(22, weight: .bold).monospacedDigit())
            }
        }
    }

    private var saveCard: some View {
        card(title: nil) {
            Button(action: { store.saveTapped() }) {
                Text("Guardar")
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
            }
            .buttonStyle(PrimaryActionButtonStyle(tint: .barksRed))
            .disabled(!store.canSave)
        }
    }

    @ViewBuilder
    private var deleteCard: some View {
        if store.isEditing {
            card(title: nil) {
                Button("Eliminar venta") {
                    store.deleteTapped()
                }
                .buttonStyle(DestructiveActionButtonStyle(tint: .barksRed))
            }
        }
    }

    // MARK: - Card Container

    private func card(title: String?, @ViewBuilder content: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            if let title {
                Text(title)
                    .font(.omnes(15, weight: .semiBold))
                    .foregroundStyle(primaryText.opacity(0.85))
            }
            content()
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(cardBackground)
        )
        .shadow(color: .black.opacity(0.08), radius: 16, y: 10)
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
}

// MARK: - Button Styles

private struct PrimaryActionButtonStyle: ButtonStyle {
    let tint: Color
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .background(tint.opacity(configuration.isPressed ? 0.85 : 1))
            .foregroundStyle(Color.barksWhite)
            .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

private struct DestructiveActionButtonStyle: ButtonStyle {
    let tint: Color
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(tint, lineWidth: 1.5)
            )
            .foregroundStyle(tint)
    }
}
