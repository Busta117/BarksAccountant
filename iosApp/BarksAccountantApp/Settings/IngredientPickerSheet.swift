import SwiftUI
import Shared

struct IngredientPickerSheet: View {
    let ingredients: [Ingredient]
    var onSelected: (Ingredient) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            if ingredients.isEmpty {
                VStack(spacing: 12) {
                    Text("No hay ingredientes en el catálogo")
                        .font(.omnes(16, weight: .semiBold))
                    Text("Créalos en Settings → Ingredientes.")
                        .font(.omnes(13))
                        .foregroundStyle(.secondary)
                }
                .padding()
                .navigationTitle("Elegir ingrediente")
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Button("Cerrar") { dismiss() }
                    }
                }
            } else {
                List(ingredients, id: \.id) { ingredient in
                    Button {
                        onSelected(ingredient)
                        dismiss()
                    } label: {
                        HStack {
                            Text(ingredient.name)
                                .font(.omnes(17))
                                .foregroundStyle(Color.barksPrincipal)
                            Spacer()
                            Text(ingredient.unit.symbol)
                                .font(.omnes(13, weight: .semiBold))
                                .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(Color.gray.opacity(0.15))
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }
                    }
                }
                .navigationTitle("Elegir ingrediente")
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Button("Cerrar") { dismiss() }
                    }
                }
            }
        }
    }
}
