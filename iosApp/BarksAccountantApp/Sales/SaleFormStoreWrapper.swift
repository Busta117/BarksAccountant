import Foundation
import Shared

@Observable
final class SaleFormStoreWrapper {
    private(set) var isEditing: Bool = false
    private(set) var clientName: String = ""
    private(set) var orderDate: String = ""
    private(set) var deliveryDate: String? = nil
    private(set) var products: [SaleProduct] = []
    private(set) var clients: [Client] = []
    private(set) var availableProducts: [Product] = []
    private(set) var isSaving: Bool = false
    private(set) var savedSuccessfully: Bool = false
    private(set) var showDeleteConfirm: Bool = false
    private(set) var deletedSuccessfully: Bool = false
    private(set) var error: String? = nil
    private(set) var totalPrice: Double = 0.0
    private(set) var canSave: Bool = false

    private let store: SaleFormStore
    private var collector: FlowCollector<SaleFormState>?

    init(saleRepository: SaleRepository, productRepository: ProductRepository, clientRepository: ClientRepository, personName: String) {
        self.store = SaleFormStore(
            saleRepository: saleRepository,
            productRepository: productRepository,
            clientRepository: clientRepository,
            createdBy: personName
        )
    }

    func start(saleId: String?) {
        collector = FlowCollector<SaleFormState>(
            flow: store.state,
            callback: { [weak self] state in
                guard let self else { return }
                self.isEditing = state.isEditing
                self.clientName = state.clientName
                self.orderDate = state.orderDate
                self.deliveryDate = state.deliveryDate
                self.products = state.products as? [SaleProduct] ?? []
                self.clients = state.clients as? [Client] ?? []
                self.availableProducts = state.availableProducts as? [Product] ?? []
                self.isSaving = state.isSaving
                self.savedSuccessfully = state.savedSuccessfully
                self.showDeleteConfirm = state.showDeleteConfirm
                self.deletedSuccessfully = state.deletedSuccessfully
                self.error = state.error
                self.totalPrice = state.totalPrice
                self.canSave = state.canSave
            }
        )
        store.dispatch(message: SaleFormMessageStarted(saleId: saleId))
    }

    func clientSelected(_ name: String) {
        store.dispatch(message: SaleFormMessageClientSelected(name: name))
    }

    func orderDateChanged(_ date: String) {
        store.dispatch(message: SaleFormMessageOrderDateChanged(date: date))
    }

    func deliveryDateChanged(_ date: String?) {
        store.dispatch(message: SaleFormMessageDeliveryDateChanged(date: date))
    }

    func addProduct(_ product: Product) {
        store.dispatch(message: SaleFormMessageAddProduct(product: product))
    }

    func removeProduct(at index: Int) {
        store.dispatch(message: SaleFormMessageRemoveProduct(index: Int32(index)))
    }

    func incrementQuantity(at index: Int) {
        store.dispatch(message: SaleFormMessageIncrementQuantity(index: Int32(index)))
    }

    func decrementQuantity(at index: Int) {
        store.dispatch(message: SaleFormMessageDecrementQuantity(index: Int32(index)))
    }

    func saveTapped() {
        store.dispatch(message: SaleFormMessageSaveTapped.shared)
    }

    func deleteTapped() {
        store.dispatch(message: SaleFormMessageDeleteTapped.shared)
    }

    func confirmDelete() {
        store.dispatch(message: SaleFormMessageConfirmDelete.shared)
    }

    func dismissDelete() {
        store.dispatch(message: SaleFormMessageDismissDelete.shared)
    }

    deinit {
        collector?.close()
        store.dispose()
    }
}
