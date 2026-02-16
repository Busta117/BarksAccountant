import SwiftUI
import Shared

struct PurchasesListView: View {
    let serviceLocator: ServiceLocator
    let personName: String
    @State private var store: PurchasesListStoreWrapper

    init(serviceLocator: ServiceLocator, personName: String) {
        self.serviceLocator = serviceLocator
        self.personName = personName
        _store = State(initialValue: PurchasesListStoreWrapper(purchaseRepository: serviceLocator.purchaseRepository))
    }

    var body: some View {
        Group {
            if store.isLoading && store.purchases.isEmpty {
                ProgressView()
            } else if let error = store.error, store.purchases.isEmpty {
                VStack(spacing: 12) {
                    Text(error)
                        .foregroundStyle(.secondary)
                    Button("Reintentar") {
                        store.reload()
                    }
                }
            } else if store.purchases.isEmpty {
                Text("No hay compras")
                    .font(.title3)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(store.purchases, id: \.id) { purchase in
                    PurchaseRow(purchase: purchase)
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            NavigationLink(value: purchase.id) {
                                Text("Editar")
                            }
                            .tint(.blue)
                        }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Compras")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink(value: "new_purchase") {
                    Image(systemName: "plus")
                }
            }
        }
        .navigationDestination(for: String.self) { value in
            if value == "new_purchase" {
                PurchaseFormView(serviceLocator: serviceLocator, purchaseId: nil, personName: personName, onSaved: {
                    store.reload()
                })
            } else {
                PurchaseFormView(serviceLocator: serviceLocator, purchaseId: value, personName: personName, onSaved: {
                    store.reload()
                })
            }
        }
        .onAppear { store.start() }
    }
}

struct PurchaseRow: View {
    let purchase: Purchase

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(purchase.title)
                .font(.headline)
            if let desc = purchase.description_, !desc.isEmpty {
                Text(desc)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
            HStack {
                Text(purchase.date)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Spacer()
                Text(String(format: "€%.2f", purchase.value))
                    .font(.subheadline)
                    .fontWeight(.semibold)
            }
        }
        .padding(.vertical, 4)
    }
}
