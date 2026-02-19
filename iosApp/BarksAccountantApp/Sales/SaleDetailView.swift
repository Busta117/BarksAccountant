import SwiftUI
import Shared

struct SaleDetailView: View {
    let serviceLocator: ServiceLocator
    let saleId: String
    let personName: String
    var onSaleUpdated: () -> Void

    @State private var store: SaleDetailStoreWrapper
    @State private var showEditForm = false
    @State private var showInvoice = false
    @State private var showSummary = false

    @Environment(\.colorScheme) private var colorScheme

    init(
        serviceLocator: ServiceLocator,
        saleId: String,
        personName: String,
        onSaleUpdated: @escaping () -> Void
    ) {
        self.serviceLocator = serviceLocator
        self.saleId = saleId
        self.personName = personName
        self.onSaleUpdated = onSaleUpdated
        _store = State(initialValue: SaleDetailStoreWrapper(
            saleRepository: serviceLocator.saleRepository,
            clientRepository: serviceLocator.clientRepository,
            businessInfoRepository: serviceLocator.businessInfoRepository
        ))
    }

    /// Screen background depending on appearance mode
    private var screenBackground: Color {
        colorScheme == .dark ? .barksBlack : .barksWhite
    }

    /// Card background used across the entire screen
    private var cardBackground: Color {
        if colorScheme == .dark {
            return Color.white.opacity(0.06)
        } else {
            return Color.barksLightBlue.opacity(0.25)
        }
    }

    /// Primary text color depending on appearance mode
    private var primaryText: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    /// Secondary text color depending on appearance mode
    private var secondaryText: Color {
        primaryText.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    var body: some View {
        ZStack {
            screenBackground
                .ignoresSafeArea()

            content
                .navigationTitle("Detalle de Venta")
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button(action: { showEditForm = true }) {
                            Image(systemName: "pencil")
                        }
                        .foregroundStyle(primaryText)
                    }
                }
        }
        .navigationDestination(isPresented: $showEditForm) {
            SaleFormView(
                serviceLocator: serviceLocator,
                saleId: saleId,
                personName: personName,
                onSaved: {
                    store.start(saleId: saleId)
                    onSaleUpdated()
                }
            )
        }
        .alert(
            "Marcar como pagado",
            isPresented: Binding(
                get: { store.showPayConfirm },
                set: { if !$0 { store.dismissConfirm() } }
            )
        ) {
            Button("Cancelar", role: .cancel) { store.dismissConfirm() }
            Button("Confirmar") { store.confirmPaid() }
        } message: {
            Text("¿Desea marcar esta venta como pagada?")
        }
        .alert(
            "Marcar como entregado",
            isPresented: Binding(
                get: { store.showDeliverConfirm },
                set: { if !$0 { store.dismissConfirm() } }
            )
        ) {
            Button("Cancelar", role: .cancel) { store.dismissConfirm() }
            Button("Confirmar") { store.confirmDelivered() }
        } message: {
            Text("¿Desea marcar esta venta como entregada?")
        }
        .onAppear { store.start(saleId: saleId) }
        .onChange(of: store.invoiceHtml) { _, html in
            showInvoice = html != nil
        }
        .navigationDestination(isPresented: $showInvoice) {
            if let html = store.invoiceHtml {
                InvoiceView(invoiceHtml: html, saleId: saleId)
            }
        }
        .onChange(of: store.summaryHtml) { _, html in
            showSummary = html != nil
        }
        .navigationDestination(isPresented: $showSummary) {
            if let html = store.summaryHtml {
                InvoiceView(invoiceHtml: html, saleId: saleId, documentName: "Resumen")
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        if let sale = store.sale {
            saleContent(sale)
        } else if let error = store.error {
            VStack(spacing: 12) {
                Text(error)
                    .font(.omnes(17))
                    .foregroundStyle(secondaryText)

                Button("Reintentar") { store.start(saleId: saleId) }
                    .buttonStyle(.borderedProminent)
                    .tint(.barksRed)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.horizontal, 24)
        } else {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }

    private func saleContent(_ sale: Sale) -> some View {
        ScrollView {
            VStack(spacing: 12) {
                card(title: "Cliente") {
                    infoRow(title: "Nombre", value: sale.clientName)
                }

                card(title: "Productos") {
                    let products = sale.products as? [SaleProduct] ?? []

                    VStack(spacing: 10) {
                        ForEach(Array(products.enumerated()), id: \.offset) { index, product in
                            HStack(alignment: .top, spacing: 12) {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(product.name)
                                        .font(.omnes(17, weight: .semiBold))
                                        .foregroundStyle(primaryText)
                                        .lineLimit(1)

                                    Text("€\(String(format: "%.2f", product.unitPrice)) x \(product.quantity)")
                                        .font(.omnes(13))
                                        .foregroundStyle(secondaryText)
                                        .lineLimit(1)
                                }

                                Spacer(minLength: 12)

                                Text(String(format: "€%.2f", product.totalPrice))
                                    .font(.omnes(17, weight: .semiBold))
                                    .foregroundStyle(primaryText)
                            }

                            if index != products.indices.last {
                                Divider().opacity(colorScheme == .dark ? 0.25 : 0.18)
                            }
                        }

                        Divider().opacity(colorScheme == .dark ? 0.25 : 0.18)

                        HStack(alignment: .firstTextBaseline) {
                            Text("Total")
                                .font(.omnes(18, weight: .bold))
                                .foregroundStyle(primaryText)

                            Spacer()

                            Text(String(format: "€%.2f", sale.totalPrice))
                                .font(.omnes(22, weight: .bold))
                                .foregroundStyle(primaryText)
                        }
                        .padding(.top, 2)
                    }
                }

                card(title: "Fechas") {
                    infoRow(title: "Fecha de pedido", value: sale.orderDate)

                    if let deliveryDate = sale.deliveryDate {
                        infoRow(title: "Fecha de entrega", value: deliveryDate)
                    } else {
                        infoRow(title: "Fecha de entrega", value: "Sin entregar")
                    }
                }

                card(title: "Estado") {
                    statusRow(title: "Pagado", isOn: sale.isPaid)
                    statusRow(title: "Entregado", isOn: sale.isDelivered)
                }

                card(title: nil) {
                    VStack(spacing: 10) {
                        if !sale.isPaid {
                            Button("Marcar como pagado") {
                                store.markAsPaidTapped()
                            }
                            .buttonStyle(PrimaryActionButtonStyle(tint: .barksRed))
                        }

                        if !sale.isDelivered {
                            Button("Marcar como entregado") {
                                store.markAsDeliveredTapped()
                            }
                            .buttonStyle(PrimaryActionButtonStyle(tint: .barksRed))
                        }

                        Button(store.isGeneratingInvoice ? "Generando..." : "Ver factura") {
                            store.exportInvoice()
                        }
                        .buttonStyle(SecondaryActionButtonStyle(tint: .barksLightBlue))
                        .disabled(store.isGeneratingInvoice)

                        Button(store.isGeneratingSummary ? "Generando..." : "Compartir resumen") {
                            store.shareSummary()
                        }
                        .buttonStyle(SecondaryActionButtonStyle(tint: .barksLightBlue))
                        .disabled(store.isGeneratingSummary)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
    }

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

    private func infoRow(title: String, value: String) -> some View {
        HStack(alignment: .firstTextBaseline, spacing: 12) {
            Text(title)
                .font(.omnes(15))
                .foregroundStyle(secondaryText)

            Spacer(minLength: 12)

            Text(value)
                .font(.omnes(15, weight: .semiBold))
                .foregroundStyle(primaryText)
                .multilineTextAlignment(.trailing)
        }
    }

    private func statusRow(title: String, isOn: Bool) -> some View {
        HStack(spacing: 12) {
            Text(title)
                .font(.omnes(15))
                .foregroundStyle(secondaryText)

            Spacer()

            Image(systemName: isOn ? "checkmark.circle.fill" : "xmark.circle")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(isOn ? Color.green : Color.barksRed)
        }
    }
}

private struct PrimaryActionButtonStyle: ButtonStyle {
    let tint: Color

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(tint.opacity(configuration.isPressed ? 0.85 : 1.0))
            .foregroundStyle(Color.barksWhite)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

private struct SecondaryActionButtonStyle: ButtonStyle {
    let tint: Color

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(16, weight: .semiBold))
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(Color.clear)
            .foregroundStyle(tint)
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(tint.opacity(configuration.isPressed ? 0.75 : 1.0), lineWidth: 1.5)
            )
            .contentShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}
