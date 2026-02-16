import SwiftUI
import Shared

struct ProductFormView: View {
    let serviceLocator: ServiceLocator
    let productId: String?
    var onSaved: () -> Void
    @State private var store: ProductFormStoreWrapper
    @Environment(\.dismiss) private var dismiss

    init(serviceLocator: ServiceLocator, productId: String?, onSaved: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.productId = productId
        self.onSaved = onSaved
        _store = State(initialValue: ProductFormStoreWrapper(productRepository: serviceLocator.productRepository))
    }

    var body: some View {
        Form {
            Section("Nombre") {
                TextField("Nombre", text: Binding(
                    get: { store.name },
                    set: { store.nameChanged($0) }
                ))
            }

            Section("Precio (€)") {
                TextField("0.00", text: Binding(
                    get: { store.price },
                    set: { store.priceChanged($0.replacingOccurrences(of: ",", with: ".")) }
                ))
                .keyboardType(.decimalPad)
            }

            Section {
                Button(action: { store.saveTapped() }) {
                    if store.isSaving {
                        ProgressView().frame(maxWidth: .infinity)
                    } else {
                        Text("Guardar").frame(maxWidth: .infinity)
                    }
                }
                .buttonStyle(.barks)
                .disabled(!store.canSave || store.isSaving)

                if let error = store.error {
                    Text(error).foregroundStyle(Color.barksRed).font(.omnes(12))
                }
            }

            if store.isEditing {
                Section {
                    Button("Eliminar Producto") { store.deleteTapped() }
                        .frame(maxWidth: .infinity)
                        .buttonStyle(.barksDestructive)
                }
            }
        }
        .navigationTitle(store.isEditing ? "Editar Producto" : "Nuevo Producto")
        .onAppear { store.start(productId: productId) }
        .onChange(of: store.savedSuccessfully) { _, saved in if saved { onSaved(); dismiss() } }
        .onChange(of: store.deletedSuccessfully) { _, deleted in if deleted { onSaved(); dismiss() } }
        .alert("Eliminar Producto", isPresented: Binding(
            get: { store.showDeleteConfirm },
            set: { if !$0 { store.dismissDelete() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        } message: {
            Text("¿Estás seguro de que quieres eliminar este producto?")
        }
    }
}
