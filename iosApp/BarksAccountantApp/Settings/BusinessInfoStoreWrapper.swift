import Foundation
import Shared

@Observable
final class BusinessInfoStoreWrapper {
    private(set) var businessName: String = ""
    private(set) var nif: String = ""
    private(set) var address: String = ""
    private(set) var phone: String = ""
    private(set) var email: String = ""
    private(set) var bankName: String = ""
    private(set) var iban: String = ""
    private(set) var bankHolder: String = ""
    private(set) var isSaving: Bool = false
    private(set) var savedSuccessfully: Bool = false
    private(set) var error: String? = nil
    private(set) var canSave: Bool = false

    private let store: BusinessInfoStore
    private var collector: FlowCollector<BusinessInfoState>?

    init(businessInfoRepository: BusinessInfoRepository) {
        self.store = BusinessInfoStore(businessInfoRepository: businessInfoRepository)
    }

    func start() {
        collector = FlowCollector<BusinessInfoState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.businessName = state.businessName
                self.nif = state.nif
                self.address = state.address
                self.phone = state.phone
                self.email = state.email
                self.bankName = state.bankName
                self.iban = state.iban
                self.bankHolder = state.bankHolder
                self.isSaving = state.isSaving
                self.savedSuccessfully = state.savedSuccessfully
                self.error = state.error
                self.canSave = state.canSave
            }
        )
        store.dispatch(message: BusinessInfoMessageStarted.shared)
    }

    func businessNameChanged(_ text: String) { store.dispatch(message: BusinessInfoMessageBusinessNameChanged(text: text)) }
    func nifChanged(_ text: String) { store.dispatch(message: BusinessInfoMessageNifChanged(text: text)) }
    func addressChanged(_ text: String) { store.dispatch(message: BusinessInfoMessageAddressChanged(text: text)) }
    func phoneChanged(_ text: String) { store.dispatch(message: BusinessInfoMessagePhoneChanged(text: text)) }
    func emailChanged(_ text: String) { store.dispatch(message: BusinessInfoMessageEmailChanged(text: text)) }
    func bankNameChanged(_ text: String) { store.dispatch(message: BusinessInfoMessageBankNameChanged(text: text)) }
    func ibanChanged(_ text: String) { store.dispatch(message: BusinessInfoMessageIbanChanged(text: text)) }
    func bankHolderChanged(_ text: String) { store.dispatch(message: BusinessInfoMessageBankHolderChanged(text: text)) }
    func saveTapped() { store.dispatch(message: BusinessInfoMessageSaveTapped.shared) }

    deinit {
        collector?.close()
        store.dispose()
    }
}
