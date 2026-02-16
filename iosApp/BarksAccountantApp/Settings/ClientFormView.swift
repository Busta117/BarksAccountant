import SwiftUI
import Shared

struct ClientFormView: View {
    let serviceLocator: ServiceLocator
    let clientId: String?
    var onSaved: () -> Void
    @State private var store: ClientFormStoreWrapper
    @Environment(\.dismiss) private var dismiss

    init(serviceLocator: ServiceLocator, clientId: String?, onSaved: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.clientId = clientId
        self.onSaved = onSaved
        _store = State(initialValue: ClientFormStoreWrapper(clientRepository: serviceLocator.clientRepository))
    }

    var body: some View {
        Form {
            Section("Nombre") {
                TextField("Nombre", text: Binding(
                    get: { store.name },
                    set: { store.nameChanged($0) }
                ))
            }

            Section("Responsable (opcional)") {
                TextField("Responsable", text: Binding(
                    get: { store.responsible },
                    set: { store.responsibleChanged($0) }
                ))
            }

            Section("NIF (opcional)") {
                TextField("NIF", text: Binding(
                    get: { store.nif },
                    set: { store.nifChanged($0) }
                ))
            }

            Section("Dirección (opcional)") {
                TextField("Dirección", text: Binding(
                    get: { store.address },
                    set: { store.addressChanged($0) }
                ))
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
                    Button("Eliminar Cliente") { store.deleteTapped() }
                        .frame(maxWidth: .infinity)
                        .buttonStyle(.barksDestructive)
                }
            }
        }
        .navigationTitle(store.isEditing ? "Editar Cliente" : "Nuevo Cliente")
        .onAppear { store.start(clientId: clientId) }
        .onChange(of: store.savedSuccessfully) { _, saved in if saved { onSaved(); dismiss() } }
        .onChange(of: store.deletedSuccessfully) { _, deleted in if deleted { onSaved(); dismiss() } }
        .alert("Eliminar Cliente", isPresented: Binding(
            get: { store.showDeleteConfirm },
            set: { if !$0 { store.dismissDelete() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        } message: {
            Text("¿Estás seguro de que quieres eliminar este cliente?")
        }
    }
}
