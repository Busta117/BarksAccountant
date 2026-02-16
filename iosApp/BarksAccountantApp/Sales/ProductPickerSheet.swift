import SwiftUI
import Shared

struct ProductPickerSheet: View {
    let products: [Product]
    var onSelected: (Product) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(products, id: \.id) { product in
                Button(action: {
                    onSelected(product)
                    dismiss()
                }) {
                    HStack {
                        Text(product.name)
                            .foregroundStyle(.primary)
                        Spacer()
                        Text(String(format: "€%.2f", product.unitPrice))
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Agregar Producto")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancelar") { dismiss() }
                }
            }
        }
    }
}
