import SwiftUI
import Shared

struct PurchaseFormView: View {
    let serviceLocator: ServiceLocator
    let purchaseId: String?
    let personName: String
    var onSaved: () -> Void

    @State private var store: PurchaseFormStoreWrapper
    @State private var dateValue = Date()
    @State private var amountValue: Double = 0

    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    init(
        serviceLocator: ServiceLocator,
        purchaseId: String?,
        personName: String,
        onSaved: @escaping () -> Void
    ) {
        self.serviceLocator = serviceLocator
        self.purchaseId = purchaseId
        self.personName = personName
        self.onSaved = onSaved
        _store = State(initialValue: PurchaseFormStoreWrapper(
            purchaseRepository: serviceLocator.purchaseRepository,
            personName: personName
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
                    dateCard
                    saveCard
                    deleteCard
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .navigationTitle(store.isEditing ? "Editar Compra" : "Nueva Compra")
        }
        .onAppear {
            store.start(purchaseId: purchaseId)
            amountValue = parseRawValue(store.value)
        }
        // Keep DatePicker in sync when editing an existing purchase
        .onChange(of: store.date) { _, newValue in
            guard let parsed = parseDate(newValue) else { return }
            if !Calendar.current.isDate(parsed, inSameDayAs: dateValue) {
                dateValue = parsed
            }
        }
        .onChange(of: dateValue) { _, newDate in
            store.dateChanged(formatDate(newDate))
        }
        .onChange(of: store.savedSuccessfully) { _, saved in
            if saved { onSaved(); dismiss() }
        }
        .onChange(of: store.deletedSuccessfully) { _, deleted in
            if deleted { onSaved(); dismiss() }
        }
        .onChange(of: store.value) { _, newValue in
            let parsed = parseRawValue(newValue)
            if parsed != amountValue {
                amountValue = parsed
            }
        }
        .onChange(of: amountValue) { _, newValue in
            store.valueChanged(toRawDotDecimalString(newValue))
        }
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

    // MARK: - Cards

    private var infoCard: some View {
        card(title: "Información") {
            VStack(spacing: 14) {
                // Title
                VStack(alignment: .leading, spacing: 6) {
                    Text("Título")
                        .font(.omnes(13))
                        .foregroundStyle(secondaryText)

                    TextField(
                        "Ej: Empaque, ingredientes, hielo...",
                        text: Binding(
                            get: { store.title },
                            set: { store.titleChanged($0) }
                        )
                    )
                    .font(.omnes(17, weight: .semiBold))
                    .foregroundStyle(primaryText)
                    .textInputAutocapitalization(.sentences)
                    .disableAutocorrection(false)
                    .padding(.horizontal, 12)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(fieldBackground)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(store.title.isEmpty ? Color.barksRed.opacity(0.45) : fieldBorder, lineWidth: 1)
                    )
                }

                // Description (optional)
                VStack(alignment: .leading, spacing: 6) {
                    Text("Descripción (opcional)")
                        .font(.omnes(13))
                        .foregroundStyle(secondaryText)

                    TextField(
                        "Descripción",
                        text: Binding(
                            get: { store.purchaseDescription },
                            set: { store.descriptionChanged($0) }
                        ),
                        axis: .vertical
                    )
                    .lineLimit(1...3)
                    .font(.omnes(16))
                    .foregroundStyle(primaryText)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .background(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(fieldBackground)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .stroke(fieldBorder, lineWidth: 1)
                    )
                }

                // Value
                VStack(alignment: .leading, spacing: 6) {
                    Text("Valor")
                        .font(.omnes(13))
                        .foregroundStyle(secondaryText)

                    HStack(spacing: 8) {
                        Text("€")
                            .font(.omnes(17, weight: .semiBold))
                            .foregroundStyle(secondaryText)

                        TextField(
                            "0.00",
                            value: $amountValue,
                            format: .number
                                .precision(.fractionLength(2))
                                .grouping(.automatic)
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
                            .stroke(store.value.isEmpty ? Color.barksRed.opacity(0.45) : fieldBorder, lineWidth: 1)
                    )
                }
            }
        }
    }

    private var dateCard: some View {
        card(title: "Fecha") {
            DatePicker("Fecha", selection: $dateValue, displayedComponents: .date)
                .datePickerStyle(.compact)
                .tint(.barksRed)
                .foregroundStyle(primaryText)
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
                Button("Eliminar Compra") {
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

    // MARK: - Date Helpers

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }

    private func parseDate(_ text: String) -> Date? {
        guard !text.isEmpty else { return nil }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.date(from: text)
    }
    
    private func toRawDotDecimalString(_ value: Double) -> String {
        // Always uses "." as decimal separator, no grouping separators.
        // Ensures stable persistence format for your store.
        String(format: "%.2f", value)
    }

    private func parseRawValue(_ text: String) -> Double {
        // Accepts raw store format: "1234.56"
        Double(text) ?? 0
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
