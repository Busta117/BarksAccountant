import SwiftUI
import Shared

struct ClientPickerSheet: View {
    let clients: [Client]
    var onSelected: (String) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(clients, id: \.id) { client in
                Button(action: {
                    onSelected(client.name)
                    dismiss()
                }) {
                    Text(client.name)
                        .foregroundStyle(.primary)
                }
            }
            .navigationTitle("Seleccionar Cliente")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancelar") { dismiss() }
                }
            }
        }
    }
}
