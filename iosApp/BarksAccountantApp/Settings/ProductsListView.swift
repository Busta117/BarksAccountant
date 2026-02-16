import SwiftUI
import Shared

struct ProductsListView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ProductsListStoreWrapper

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ProductsListStoreWrapper(productRepository: serviceLocator.productRepository))
    }

    var body: some View {
        Group {
            if store.isLoading && store.products.isEmpty {
                ProgressView()
            } else if let error = store.error, store.products.isEmpty {
                VStack(spacing: 12) {
                    Text(error).foregroundStyle(.secondary)
                    Button("Reintentar") { store.reload() }
                }
            } else if store.products.isEmpty {
                Text("No hay productos")
                    .font(.title3)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(store.products, id: \.id) { product in
                    NavigationLink(value: ProductDestination.edit(product.id)) {
                        HStack {
                            Text(product.name)
                            Spacer()
                            Text(String(format: "€%.2f", product.unitPrice))
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Productos")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink(value: ProductDestination.new) {
                    Image(systemName: "plus")
                }
            }
        }
        .navigationDestination(for: ProductDestination.self) { destination in
            switch destination {
            case .new:
                ProductFormView(serviceLocator: serviceLocator, productId: nil, onSaved: { store.reload() })
            case .edit(let productId):
                ProductFormView(serviceLocator: serviceLocator, productId: productId, onSaved: { store.reload() })
            }
        }
        .onAppear { store.start() }
    }
}
