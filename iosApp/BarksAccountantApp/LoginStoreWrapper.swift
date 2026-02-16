import Foundation
import Shared

@Observable
final class LoginStoreWrapper {
    private(set) var userId: String = ""
    private(set) var isLoading: Bool = false
    private(set) var error: String? = nil
    private(set) var loginSuccess: Bool = false

    private let store: LoginStore
    private var collector: FlowCollector<LoginState>?

    init(userRepository: UserRepository, initialUserId: String = "") {
        self.store = LoginStore(userRepository: userRepository, initialUserId: initialUserId)
    }

    func start() {
        collector = FlowCollector<LoginState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.userId = state.userId
                self.isLoading = state.isLoading
                self.error = state.error
                self.loginSuccess = state.loginSuccess
            }
        )
    }

    func userIdChanged(_ text: String) {
        store.dispatch(message: LoginMessageUserIdChanged(text: text))
    }

    func loginTapped() {
        store.dispatch(message: LoginMessageLoginTapped.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
