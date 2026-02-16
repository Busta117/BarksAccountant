import SwiftUI
import Shared

struct SaleDetailView: View {
    let serviceLocator: ServiceLocator
    let saleId: String
    let personName: String
    var onSaleUpdated: () -> Void
    @State private var store: SaleDetailStoreWrapper
    @State private var showEditForm = false

    init(serviceLocator: ServiceLocator, saleId: String, personName: String, onSaleUpdated: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.saleId = saleId
        self.personName = personName
        self.onSaleUpdated = onSaleUpdated
        _store = State(initialValue: SaleDetailStoreWrapper(saleRepository: serviceLocator.saleRepository))
    }

    var body: some View {
        Group {
            if let sale = store.sale {
                saleContent(sale)
            } else if let error = store.error {
                Text(error)
                    .foregroundStyle(.secondary)
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Detalle de Venta")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: { showEditForm = true }) {
                    Image(systemName: "pencil")
                }
            }
        }
        .navigationDestination(isPresented: $showEditForm) {
            SaleFormView(serviceLocator: serviceLocator, saleId: saleId, personName: personName, onSaved: {
                store.start(saleId: saleId)
                onSaleUpdated()
            })
        }
        .alert("Marcar como pagado", isPresented: Binding(
            get: { store.showPayConfirm },
            set: { if !$0 { store.dismissConfirm() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissConfirm() }
            Button("Confirmar") { store.confirmPaid() }
        } message: {
            Text("¿Desea marcar esta venta como pagada?")
        }
        .alert("Marcar como entregado", isPresented: Binding(
            get: { store.showDeliverConfirm },
            set: { if !$0 { store.dismissConfirm() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissConfirm() }
            Button("Confirmar") { store.confirmDelivered() }
        } message: {
            Text("¿Desea marcar esta venta como entregada?")
        }
        .onAppear { store.start(saleId: saleId) }
    }

    @ViewBuilder
    private func saleContent(_ sale: Sale) -> some View {
        List {
            Section("Cliente") {
                LabeledContent("Nombre", value: sale.clientName)
                if !sale.createdBy.isEmpty {
                    LabeledContent("Creado por", value: sale.createdBy)
                }
            }

            Section("Productos") {
                let products = sale.products as? [SaleProduct] ?? []
                ForEach(Array(products.enumerated()), id: \.offset) { _, product in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(product.name)
                                .font(.body)
                            Text("€\(String(format: "%.2f", product.unitPrice)) x \(product.quantity)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                        Text(String(format: "€%.2f", product.totalPrice))
                            .fontWeight(.medium)
                    }
                }
                HStack {
                    Text("Total")
                        .fontWeight(.bold)
                    Spacer()
                    Text(String(format: "€%.2f", sale.totalPrice))
                        .fontWeight(.bold)
                }
            }

            Section("Fechas") {
                LabeledContent("Fecha de pedido", value: sale.orderDate)
                if let deliveryDate = sale.deliveryDate {
                    LabeledContent("Fecha de entrega", value: deliveryDate)
                } else {
                    LabeledContent("Fecha de entrega", value: "Sin entregar")
                }
            }

            Section("Estado") {
                HStack {
                    Text("Pagado")
                    Spacer()
                    Image(systemName: sale.isPaid ? "checkmark.circle.fill" : "xmark.circle")
                        .foregroundStyle(sale.isPaid ? .green : .red)
                }
                HStack {
                    Text("Entregado")
                    Spacer()
                    Image(systemName: sale.isDelivered ? "checkmark.circle.fill" : "xmark.circle")
                        .foregroundStyle(sale.isDelivered ? .green : .red)
                }
            }

            Section {
                if !sale.isPaid {
                    Button("Marcar como pagado") {
                        store.markAsPaidTapped()
                    }
                    .frame(maxWidth: .infinity)
                }

                if !sale.isDelivered {
                    Button("Marcar como entregado") {
                        store.markAsDeliveredTapped()
                    }
                    .frame(maxWidth: .infinity)
                }

                Button("Exportar") {
                    // TODO: Implementar exportación
                }
                .frame(maxWidth: .infinity)
                .disabled(true)
            }
        }
    }
}
