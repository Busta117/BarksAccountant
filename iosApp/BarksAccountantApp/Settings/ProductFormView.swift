import SwiftUI
import Shared

struct ProductFormView: View {
    let serviceLocator: ServiceLocator
    let productId: String?
    var onSaved: () -> Void

    @State private var store: ProductFormStoreWrapper

    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator, productId: String?, onSaved: @escaping () -> Void) {
        self.serviceLocator = serviceLocator
        self.productId = productId
        self.onSaved = onSaved
        _store = State(initialValue: ProductFormStoreWrapper(
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
            .navigationTitle(store.isEditing ? "Editar Producto" : "Nuevo Producto")
        }
        .alert("Eliminar producto", isPresented: Binding(
            get: { store.showDeleteConfirm },
            set: { if !$0 { store.dismissDelete() } }
        )) {
            Button("Cancelar", role: .cancel) { store.dismissDelete() }
            Button("Eliminar", role: .destructive) { store.confirmDelete() }
        } message: {
            Text("¿Estás seguro de que quieres eliminar este producto?")
        }
        .onAppear {
            store.start(productId: productId)
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
                        "Ej: Paleta Sandía",
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

                // Price
                VStack(alignment: .leading, spacing: 6) {
                    Text("Precio")
                        .font(.omnes(13))
                        .foregroundStyle(secondaryText)

                    HStack(spacing: 8) {
                        Text("€")
                            .font(.omnes(17, weight: .semiBold))
                            .foregroundStyle(secondaryText)

                        TextField(
                            "0.00",
                            text: Binding(
                                get: { store.price },
                                set: { store.priceChanged($0) }
                            )
                        )
                        .keyboardType(.decimalPad)
                        .font(.omnes(17, weight: .semiBold).monospacedDigit())
                        .foregroundStyle(primaryText)
                    }
                    .padding(.horizontal, 12)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(fieldBackground)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(store.price.isEmpty ? Color.barksRed.opacity(0.45) : fieldBorder, lineWidth: 1)
                    )
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
                .buttonStyle(PrimaryActionButtonStyle(tint: .barksRed))
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
                Button("Eliminar producto") {
                    store.deleteTapped()
                }
                .buttonStyle(DestructiveActionButtonStyle(tint: .barksRed))
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

private struct PrimaryActionButtonStyle: ButtonStyle {
    let tint: Color

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .background(tint.opacity(configuration.isPressed ? 0.85 : 1.0))
            .foregroundStyle(Color.barksWhite)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

private struct DestructiveActionButtonStyle: ButtonStyle {
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
