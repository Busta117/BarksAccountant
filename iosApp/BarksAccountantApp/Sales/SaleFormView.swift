import SwiftUI
import Shared

struct SaleFormView: View {
    let serviceLocator: ServiceLocator
    let saleId: String?
    var onSaved: () -> Void
    @State private var store: SaleFormStoreWrapper
    @State private var showClientPicker = false
    @State private var showProductPicker = false
    @State private var showOrderDatePicker = false
    @State private var showDeliveryDatePicker = false
    @State private var orderDateValue = Date()
    @State private var deliveryDateValue = Date()
    @State private var hasDeliveryDate = false
    @Environment(\.dismiss) private var dismiss

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

    var body: some View {
        Form {
            clientSection
            datesSection
            productsSection
            totalSection
            saveSection
            deleteSection
        }
        .navigationTitle(store.isEditing ? "Editar Venta" : "Nueva Venta")
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
        .onAppear { store.start(saleId: saleId) }
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
        .alert("Eliminar Venta", isPresented: Binding(
            get: { store.showDeleteConfirm },
            set: { if !$0 { store.dismissDelete() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        } message: {
            Text("¿Estás seguro de que quieres eliminar esta venta?")
        }
    }

    // MARK: - Sections

    private var clientSection: some View {
        Section("Cliente") {
            Button(action: { showClientPicker = true }) {
                HStack {
                    Text(store.clientName.isEmpty ? "Seleccionar cliente" : store.clientName)
                        .foregroundStyle(store.clientName.isEmpty ? .secondary : .primary)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .foregroundStyle(.secondary)
                        .font(.caption)
                }
            }
        }
    }

    private var datesSection: some View {
        Section("Fechas") {
            DatePicker("Fecha de pedido", selection: $orderDateValue, displayedComponents: .date)
                .onChange(of: orderDateValue) { _, newDate in
                    store.orderDateChanged(formatDate(newDate))
                }

            Toggle("Fecha de entrega", isOn: $hasDeliveryDate)
                .onChange(of: hasDeliveryDate) { _, has in
                    if has {
                        store.deliveryDateChanged(formatDate(deliveryDateValue))
                    } else {
                        store.deliveryDateChanged(nil)
                    }
                }

            if hasDeliveryDate {
                DatePicker("Entrega", selection: $deliveryDateValue, displayedComponents: .date)
                    .onChange(of: deliveryDateValue) { _, newDate in
                        store.deliveryDateChanged(formatDate(newDate))
                    }
            }
        }
    }

    private var productsSection: some View {
        Section("Productos") {
            ForEach(Array(store.products.enumerated()), id: \.offset) { index, product in
                HStack {
                    VStack(alignment: .leading) {
                        Text(product.name)
                            .font(.body)
                        Text(String(format: "€%.2f", product.unitPrice))
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    HStack(spacing: 12) {
                        Button(action: {
                            store.decrementQuantity(at: index)
                        }) {
                            Image(systemName: product.quantity <= 1 ? "trash" : "minus.circle")
                                .foregroundStyle(product.quantity <= 1 ? .red : .blue)
                        }
                        .buttonStyle(.borderless)

                        Text("\(product.quantity)")
                            .frame(minWidth: 24)
                            .font(.body.monospacedDigit())

                        Button(action: {
                            store.incrementQuantity(at: index)
                        }) {
                            Image(systemName: "plus.circle")
                        }
                        .buttonStyle(.borderless)

                        Text(String(format: "€%.2f", product.totalPrice))
                            .font(.body)
                            .fontWeight(.medium)
                            .frame(minWidth: 60, alignment: .trailing)
                    }
                }
            }

            Button(action: { showProductPicker = true }) {
                Label("Agregar Producto", systemImage: "plus")
            }
        }
    }

    private var totalSection: some View {
        Section {
            HStack {
                Text("Total")
                    .fontWeight(.bold)
                Spacer()
                Text(String(format: "€%.2f", store.totalPrice))
                    .fontWeight(.bold)
                    .font(.title3)
            }
        }
    }

    private var saveSection: some View {
        Section {
            Button(action: { store.saveTapped() }) {
                if store.isSaving {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Guardar")
                        .frame(maxWidth: .infinity)
                        .fontWeight(.semibold)
                }
            }
            .disabled(!store.canSave || store.isSaving)

            if let error = store.error {
                Text(error)
                    .foregroundStyle(.red)
                    .font(.caption)
            }
        }
    }

    @ViewBuilder
    private var deleteSection: some View {
        if store.isEditing {
            Section {
                Button("Eliminar Venta", role: .destructive) { store.deleteTapped() }
                    .frame(maxWidth: .infinity)
            }
        }
    }

    // MARK: - Helpers

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
}
