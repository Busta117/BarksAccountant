import SwiftUI
import Shared

struct SalesListView: View {
    let serviceLocator: ServiceLocator
    @State private var store: SalesListStoreWrapper

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: SalesListStoreWrapper(saleRepository: serviceLocator.saleRepository))
    }

    var body: some View {
        Group {
            if store.isLoading && store.sales.isEmpty {
                ProgressView()
            } else if let error = store.error, store.sales.isEmpty {
                VStack(spacing: 12) {
                    Text(error)
                        .foregroundStyle(.secondary)
                    Button("Reintentar") {
                        store.reload()
                    }
                }
            } else {
                List(store.sales, id: \.id) { sale in
                    NavigationLink(value: sale.id) {
                        SaleRow(sale: sale)
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Ventas")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink(value: "new_sale") {
                    Image(systemName: "plus")
                }
            }
        }
        .navigationDestination(for: String.self) { value in
            if value == "new_sale" {
                SaleFormView(serviceLocator: serviceLocator, saleId: nil, onSaved: {
                    store.reload()
                })
            } else {
                SaleDetailView(serviceLocator: serviceLocator, saleId: value, onSaleUpdated: {
                    store.reload()
                })
            }
        }
        .onAppear { store.start() }
    }
}

struct SaleRow: View {
    let sale: Sale

    var body: some View {
        HStack(spacing: 0) {
            if !sale.isPaid {
                Rectangle()
                    .fill(Color.red)
                    .frame(width: 4)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(sale.clientName)
                    .font(.headline)
                HStack {
                    Text(sale.orderDate)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(String(format: "€%.2f", sale.totalPrice))
                        .font(.subheadline)
                        .fontWeight(.semibold)
                }
            }
            .padding(.vertical, 4)
            .padding(.leading, sale.isPaid ? 0 : 8)
        }
    }
}
