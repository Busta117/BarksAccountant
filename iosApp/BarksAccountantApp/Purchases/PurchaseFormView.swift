import SwiftUI
import Shared

struct PurchaseFormView: View {
    let serviceLocator: ServiceLocator
    let purchaseId: String?
    let personName: String
    var onSaved: () -> Void
    @State private var store: PurchaseFormStoreWrapper
    @State private var dateValue = Date()
    @Environment(\.dismiss) private var dismiss

    init(serviceLocator: ServiceLocator, purchaseId: String?, personName: String, onSaved: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.purchaseId = purchaseId
        self.personName = personName
        self.onSaved = onSaved
        _store = State(initialValue: PurchaseFormStoreWrapper(
            purchaseRepository: serviceLocator.purchaseRepository,
            personName: personName
        ))
    }

    var body: some View {
        Form {
            Section("Titulo") {
                TextField("Titulo", text: Binding(
                    get: { store.title },
                    set: { store.titleChanged($0) }
                ))
            }

            Section("Descripción (opcional)") {
                TextField("Descripción", text: Binding(
                    get: { store.purchaseDescription },
                    set: { store.descriptionChanged($0) }
                ), axis: .vertical)
                .lineLimit(1...3)
            }

            Section("Valor (€)") {
                TextField("0.00", text: Binding(
                    get: { store.value },
                    set: { store.valueChanged($0.replacingOccurrences(of: ",", with: ".")) }
                ))
                .keyboardType(.decimalPad)
            }

            Section("Fecha") {
                DatePicker("Fecha", selection: $dateValue, displayedComponents: .date)
                    .onChange(of: dateValue) { _, newDate in
                        store.dateChanged(formatDate(newDate))
                    }
            }

            Section {
                Button(action: { store.saveTapped() }) {
                    if store.isSaving {
                        ProgressView().frame(maxWidth: .infinity)
                    } else {
                        Text("Guardar").frame(maxWidth: .infinity).fontWeight(.semibold)
                    }
                }
                .disabled(!store.canSave || store.isSaving)

                if let error = store.error {
                    Text(error).foregroundStyle(.red).font(.caption)
                }
            }

            if store.isEditing {
                Section {
                    Button("Eliminar Compra", role: .destructive) { store.deleteTapped() }
                        .frame(maxWidth: .infinity)
                }
            }
        }
        .navigationTitle(store.isEditing ? "Editar Compra" : "Nueva Compra")
        .onAppear { store.start(purchaseId: purchaseId) }
        .onChange(of: store.savedSuccessfully) { _, saved in if saved { onSaved(); dismiss() } }
        .onChange(of: store.deletedSuccessfully) { _, deleted in if deleted { onSaved(); dismiss() } }
        .alert("Eliminar Compra", isPresented: Binding(
            get: { store.showDeleteConfirm },
            set: { if !$0 { store.dismissDelete() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        } message: {
            Text("¿Estás seguro de que quieres eliminar esta compra?")
        }
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
}
