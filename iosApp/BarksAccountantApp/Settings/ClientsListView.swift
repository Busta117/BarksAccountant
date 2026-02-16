import SwiftUI
import Shared

struct ClientsListView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ClientsListStoreWrapper

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ClientsListStoreWrapper(clientRepository: serviceLocator.clientRepository))
    }

    var body: some View {
        Group {
            if store.isLoading && store.clients.isEmpty {
                ProgressView()
            } else if let error = store.error, store.clients.isEmpty {
                VStack(spacing: 12) {
                    Text(error).font(.omnes(17)).foregroundStyle(Color.barksPrincipal.opacity(0.6))
                    Button("Reintentar") { store.reload() }
                }
            } else if store.clients.isEmpty {
                Text("No hay clientes")
                    .font(.vagRundschrift(20))
                    .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(store.clients, id: \.id) { client in
                    NavigationLink(value: ClientDestination.edit(client.id)) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(client.name).font(.omnes(17)).foregroundStyle(Color.barksPrincipal)
                            if let responsible = client.responsible, !responsible.isEmpty {
                                Text(responsible)
                                    .font(.omnes(15))
                                    .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                            }
                        }
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Clientes")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink(value: ClientDestination.new) {
                    Image(systemName: "plus")
                }
            }
        }
        .navigationDestination(for: ClientDestination.self) { destination in
            switch destination {
            case .new:
                ClientFormView(serviceLocator: serviceLocator, clientId: nil, onSaved: { store.reload() })
            case .edit(let clientId):
                ClientFormView(serviceLocator: serviceLocator, clientId: clientId, onSaved: { store.reload() })
            }
        }
        .onAppear { store.start() }
    }
}
