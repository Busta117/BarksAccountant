import Foundation
import Shared

@Observable
final class AppStoreWrapper {
    private(set) var isLoggedIn: Bool = false
    private(set) var userId: String? = nil
    private(set) var isCheckingAuth: Bool = true

    private let store: AppStore
    private var collector: FlowCollector<AppState>?
    let serviceLocator: ServiceLocator

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        self.store = AppStore(localStorage: serviceLocator.localStorage)
    }

    func start() {
        collector = FlowCollector<AppState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.isLoggedIn = state.isLoggedIn
                self.userId = state.userId
                self.isCheckingAuth = state.isCheckingAuth
            }
        )
        store.dispatch(message: AppMessageCheckAuth.shared)
    }

    func onLoggedIn(userId: String) {
        store.dispatch(message: AppMessageLoggedIn(userId: userId))
    }

    func onLoggedOut() {
        store.dispatch(message: AppMessageLoggedOut.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
