import SwiftUI
import Shared

struct BusinessInfoView: View {
    let serviceLocator: ServiceLocator

    @State private var store: BusinessInfoStoreWrapper

    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: BusinessInfoStoreWrapper(
            businessInfoRepository: serviceLocator.businessInfoRepository
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

    var body: some View {
        ZStack {
            screenBackground
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 12) {
                    infoCard
                    bankCard
                    saveCard
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .navigationTitle("Datos del negocio")
        }
        .onAppear {
            store.start()
        }
        .onChange(of: store.savedSuccessfully) { _, saved in
            if saved {
                dismiss()
            }
        }
    }

    // MARK: - Cards

    private var infoCard: some View {
        card(title: "Información") {
            VStack(spacing: 14) {
                fieldGroup(label: "Nombre del negocio", placeholder: "Ej: Mi Empresa S.L.",
                           text: Binding(get: { store.businessName }, set: { store.businessNameChanged($0) }),
                           hasError: store.businessName.isEmpty)

                fieldGroup(label: "NIF (opcional)", placeholder: "Ej: B12345678",
                           text: Binding(get: { store.nif }, set: { store.nifChanged($0) }))

                fieldGroup(label: "Dirección (opcional)", placeholder: "Dirección del negocio",
                           text: Binding(get: { store.address }, set: { store.addressChanged($0) }))

                fieldGroup(label: "Teléfono (opcional)", placeholder: "Teléfono",
                           text: Binding(get: { store.phone }, set: { store.phoneChanged($0) }),
                           keyboardType: .phonePad)

                fieldGroup(label: "Email (opcional)", placeholder: "Email",
                           text: Binding(get: { store.email }, set: { store.emailChanged($0) }),
                           keyboardType: .emailAddress)
            }
        }
    }

    private var bankCard: some View {
        card(title: "Información bancaria (opcional)") {
            VStack(spacing: 14) {
                fieldGroup(label: "Banco", placeholder: "Ej: Banco Santander",
                           text: Binding(get: { store.bankName }, set: { store.bankNameChanged($0) }))

                fieldGroup(label: "IBAN", placeholder: "Ej: ES12 1234 5678 9012 3456 7890",
                           text: Binding(get: { store.iban }, set: { store.ibanChanged($0) }))

                fieldGroup(label: "Titular", placeholder: "Titular de la cuenta",
                           text: Binding(get: { store.bankHolder }, set: { store.bankHolderChanged($0) }))
            }
        }
    }

    private var saveCard: some View {
        card(title: nil) {
            VStack(spacing: 10) {
                Button(action: { store.saveTapped() }) {
                    if store.isSaving {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                    } else {
                        Text("Guardar")
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                    }
                }
                .buttonStyle(BusinessInfoPrimaryButtonStyle(tint: .barksRed))
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

    // MARK: - Field Group

    private func fieldGroup(
        label: String,
        placeholder: String,
        text: Binding<String>,
        hasError: Bool = false,
        keyboardType: UIKeyboardType = .default
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.omnes(13))
                .foregroundStyle(secondaryText)

            TextField(placeholder, text: text)
                .font(.omnes(17, weight: .semiBold))
                .foregroundStyle(primaryText)
                .keyboardType(keyboardType)
                .textInputAutocapitalization(keyboardType == .emailAddress ? .never : .words)
                .disableAutocorrection(true)
                .padding(.horizontal, 12)
                .frame(height: 48)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(
                            hasError
                            ? Color.barksRed.opacity(0.4)
                            : Color.clear,
                            lineWidth: 1
                        )
                )
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

// MARK: - Button Style

private struct BusinessInfoPrimaryButtonStyle: ButtonStyle {
    let tint: Color

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .background(tint.opacity(configuration.isPressed ? 0.85 : 1.0))
            .foregroundStyle(Color.barksWhite)
            .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
