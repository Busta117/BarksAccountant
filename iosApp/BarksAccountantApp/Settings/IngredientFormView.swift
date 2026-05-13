import SwiftUI
import Shared

struct IngredientFormView: View {
    let serviceLocator: ServiceLocator
    let ingredientId: String?
    var onSaved: () -> Void

    @State private var store: IngredientFormStoreWrapper

    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator, ingredientId: String?, onSaved: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.ingredientId = ingredientId
        self.onSaved = onSaved
        _store = State(initialValue: IngredientFormStoreWrapper(
            ingredientRepository: serviceLocator.ingredientRepository,
            productRepository: serviceLocator.productRepository
        ))
    }

    // MARK: - Theme

    private var screenBackground: Color {
        colorScheme == .dark ? .barksBlack : .barksWhite
    }

    private var cardBackground: Color {
        colorScheme == .dark
        ? Color.white.opacity(0.06)
        : Color.barksLightBlue.opacity(0.25)
    }

    private var primaryText: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    private var secondaryText: Color {
        primaryText.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    private var fieldBackground: Color {
        colorScheme == .dark ? Color.white.opacity(0.05) : Color.white.opacity(0.7)
    }

    private var fieldBorder: Color {
        colorScheme == .dark ? Color.white.opacity(0.10) : Color.black.opacity(0.06)
    }

    // MARK: - Units

    private let allUnits: [IngredientUnit] = [.grams, .kilograms, .milliliters, .liters, .units]

    private func label(_ u: IngredientUnit) -> String {
        switch u {
        case .grams: return "gramos"
        case .kilograms: return "kilos"
        case .milliliters: return "mililitros"
        case .liters: return "litros"
        case .units: return "unidades"
        default: return u.symbol
        }
    }

    var body: some View {
        ZStack {
            screenBackground
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 12) {
                    infoCard
                    saveCard
                    deleteCard
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .navigationTitle(store.isEditing ? "Editar Ingrediente" : "Nuevo Ingrediente")
        }
        .alert("Eliminar ingrediente", isPresented: Binding(
            get: { store.showDeleteConfirm },
            set: { if !$0 { store.dismissDelete() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        } message: {
            Text("¿Estás seguro de que quieres eliminar este ingrediente?")
        }
        .alert("No se puede eliminar", isPresented: Binding(
            get: { !store.deleteBlockedBy.isEmpty },
            set: { if !$0 { store.dismissDeleteBlocked() } }
        )) {
            Button("Aceptar") { store.dismissDeleteBlocked() }
        } message: {
            Text("Este ingrediente se usa en: \(store.deleteBlockedBy.joined(separator: ", ")). Elimínalo de esos productos primero.")
        }
        .onAppear {
            store.start(ingredientId: ingredientId)
        }
        .onChange(of: store.savedSuccessfully) { _, saved in
            if saved {
                onSaved()
                dismiss()
            }
        }
        .onChange(of: store.deletedSuccessfully) { _, deleted in
            if deleted {
                onSaved()
                dismiss()
            }
        }
    }

    // MARK: - Cards

    private var infoCard: some View {
        card(title: "Información") {
            VStack(spacing: 14) {

                // Name
                VStack(alignment: .leading, spacing: 6) {
                    Text("Nombre")
                        .font(.omnes(13))
                        .foregroundStyle(secondaryText)

                    TextField(
                        "Ej: Azúcar",
                        text: Binding(
                            get: { store.name },
                            set: { store.nameChanged($0) }
                        )
                    )
                    .font(.omnes(17, weight: .semiBold))
                    .foregroundStyle(primaryText)
                    .textInputAutocapitalization(.words)
                    .disableAutocorrection(true)
                    .padding(.horizontal, 12)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(fieldBackground)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(store.name.isEmpty ? Color.barksRed.opacity(0.45) : fieldBorder, lineWidth: 1)
                    )
                }

                // Unit
                VStack(alignment: .leading, spacing: 6) {
                    Text("Unidad")
                        .font(.omnes(13))
                        .foregroundStyle(secondaryText)

                    Menu {
                        ForEach(allUnits, id: \.self) { u in
                            Button("\(u.symbol) — \(label(u))") {
                                store.unitChanged(u)
                            }
                        }
                    } label: {
                        HStack {
                            Text("\(store.unit.symbol) — \(label(store.unit))")
                                .font(.omnes(17, weight: .semiBold))
                                .foregroundStyle(primaryText)
                            Spacer()
                            Image(systemName: "chevron.up.chevron.down")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundStyle(secondaryText)
                        }
                        .padding(.horizontal, 12)
                        .frame(height: 48)
                        .background(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(fieldBackground)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .stroke(fieldBorder, lineWidth: 1)
                        )
                    }
                    .disabled(store.isEditing)
                    .opacity(store.isEditing ? 0.6 : 1.0)

                    if store.isEditing {
                        Text("La unidad no se puede cambiar")
                            .font(.omnes(12))
                            .foregroundStyle(secondaryText)
                    }
                }
            }
        }
    }

    private var saveCard: some View {
        card(title: nil) {
            VStack(spacing: 10) {
                Button(action: { store.saveTapped() }) {
                    Group {
                        if store.isSaving {
                            ProgressView()
                        } else {
                            Text("Guardar")
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                }
                .buttonStyle(IngredientPrimaryActionButtonStyle(tint: .barksRed))
                .disabled(!store.canSave || store.isSaving)
                .opacity((!store.canSave || store.isSaving) ? 0.6 : 1.0)

                if let error = store.error {
                    Text(error)
                        .font(.omnes(13))
                        .foregroundStyle(Color.barksRed)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
    }

    @ViewBuilder
    private var deleteCard: some View {
        if store.isEditing {
            card(title: nil) {
                Button("Eliminar ingrediente") {
                    store.deleteTapped()
                }
                .buttonStyle(IngredientDestructiveActionButtonStyle(tint: .barksRed))
            }
        }
    }

    // MARK: - Card Container

    private func card(
        title: String?,
        @ViewBuilder content: () -> some View
    ) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            if let title {
                Text(title)
                    .font(.omnes(15, weight: .semiBold))
                    .foregroundStyle(primaryText.opacity(0.85))
            }

            content()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(cardBackground)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(Color.white.opacity(colorScheme == .dark ? 0.06 : 0.0), lineWidth: 1)
        )
        .shadow(
            color: .black.opacity(colorScheme == .dark ? 0.18 : 0.08),
            radius: 16,
            x: 0,
            y: 10
        )
    }
}

// MARK: - Button Styles

private struct IngredientPrimaryActionButtonStyle: ButtonStyle {
    let tint: Color

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .background(tint.opacity(configuration.isPressed ? 0.85 : 1.0))
            .foregroundStyle(Color.barksWhite)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

private struct IngredientDestructiveActionButtonStyle: ButtonStyle {
    let tint: Color

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(tint, lineWidth: 1.5)
            )
            .foregroundStyle(tint)
    }
}
