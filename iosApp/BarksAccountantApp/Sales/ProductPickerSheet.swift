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
                            .font(.omnes(17))
                            .foregroundStyle(Color.barksPrincipal)
                        Spacer()
                        Text(String(format: "€%.2f", product.unitPrice))
                            .font(.omnes(15))
                            .foregroundStyle(Color.barksPrincipal.opacity(0.6))
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
