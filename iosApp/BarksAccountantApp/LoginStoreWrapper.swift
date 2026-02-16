import Foundation
import Shared

@Observable
final class LoginStoreWrapper {
    private(set) var appId: String = ""
    private(set) var personName: String = ""
    private(set) var isLoading: Bool = false
    private(set) var error: String? = nil
    private(set) var loginSuccess: Bool = false

    private let store: LoginStore
    private var collector: FlowCollector<LoginState>?

    init(appIdRepository: AppIdRepository) {
        self.store = LoginStore(appIdRepository: appIdRepository)
    }

    func start() {
        collector = FlowCollector<LoginState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.appId = state.appId
                self.personName = state.personName
                self.isLoading = state.isLoading
                self.error = state.error
                self.loginSuccess = state.loginSuccess
            }
        )
    }

    func appIdChanged(_ text: String) {
        store.dispatch(message: LoginMessageAppIdChanged(text: text))
    }

    func personNameChanged(_ text: String) {
        store.dispatch(message: LoginMessagePersonNameChanged(text: text))
    }

    func loginTapped() {
        store.dispatch(message: LoginMessageLoginTapped.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
