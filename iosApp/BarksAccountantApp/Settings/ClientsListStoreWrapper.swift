import Foundation
import Shared

@Observable
final class ClientsListStoreWrapper {
    private(set) var clients: [Client] = []
    private(set) var isLoading: Bool = true
    private(set) var error: String? = nil

    private let store: ClientsListStore
    private var collector: FlowCollector<ClientsListState>?

    init(clientRepository: ClientRepository) {
        self.store = ClientsListStore(clientRepository: clientRepository)
    }

    func start() {
        collector = FlowCollector<ClientsListState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.clients = state.clients as? [Client] ?? []
                self.isLoading = state.isLoading
                self.error = state.error
            }
        )
        store.dispatch(message: ClientsListMessageStarted.shared)
    }

    func reload() {
        store.dispatch(message: ClientsListMessageStarted.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
