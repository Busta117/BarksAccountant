import SwiftUI
import Shared

enum SettingsDestination: Hashable {
    case products
    case clients
}

enum ProductDestination: Hashable {
    case new
    case edit(String)
}

enum ClientDestination: Hashable {
    case new
    case edit(String)
}

struct SettingsView: View {
    let serviceLocator: ServiceLocator
    let personName: String
    var onLogout: () -> Void

    @State private var showLogoutConfirm = false
    @Environment(\.colorScheme) private var colorScheme

    // MARK: - Theme

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

            ScrollView {
                VStack(spacing: 12) {
                    accountCard
                    managementCard
                    sessionCard
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
        }
        .navigationTitle("Settings")
        .navigationDestination(for: SettingsDestination.self) { destination in
            switch destination {
            case .products:
                ProductsListView(serviceLocator: serviceLocator)
            case .clients:
                ClientsListView(serviceLocator: serviceLocator)
            }
        }
        .alert("Cerrar sesión", isPresented: $showLogoutConfirm) {
            Button("Cancelar", role: .cancel) { }
            Button("Cerrar sesión", role: .destructive) { onLogout() }
        } message: {
            Text("¿Estás seguro de que quieres cerrar sesión?")
        }
    }

    // MARK: - Cards

    private var accountCard: some View {
        card(title: "Cuenta") {
            VStack(alignment: .leading, spacing: 10) {
                KeyValueRow(
                    title: "App ID",
                    value: serviceLocator.appId,
                    primaryText: primaryText,
                    secondaryText: secondaryText,
                    valueFont: .omnes(15, weight: .semiBold).monospaced()
                )

                Divider().opacity(colorScheme == .dark ? 0.25 : 0.18)

                KeyValueRow(
                    title: "Usuario",
                    value: personName,
                    primaryText: primaryText,
                    secondaryText: secondaryText,
                    valueFont: .omnes(16, weight: .semiBold)
                )
            }
        }
    }

    private var managementCard: some View {
        card(title: "Gestión") {
            VStack(spacing: 0) {
                NavigationLink(value: SettingsDestination.products) {
                    settingsRow(
                        icon: "tag.fill",
                        iconTint: .barksLightBlue,
                        title: "Productos",
                        subtitle: "Crea y edita tu catálogo"
                    )
                }
                .buttonStyle(.plain)

                Divider().opacity(colorScheme == .dark ? 0.25 : 0.18)

                NavigationLink(value: SettingsDestination.clients) {
                    settingsRow(
                        icon: "person.2.fill",
                        iconTint: .barksPink,
                        title: "Clientes",
                        subtitle: "Gestiona tus compradores"
                    )
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var sessionCard: some View {
        card(title: "Sesión") {
            Button {
                showLogoutConfirm = true
            } label: {
                HStack(spacing: 12) {
                    Image(systemName: "rectangle.portrait.and.arrow.right")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(Color.barksRed)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 2) {
                        Text("Cerrar sesión")
                            .font(.omnes(16, weight: .semiBold))
                            .foregroundStyle(Color.barksRed)

                        Text("Sal de tu cuenta en este dispositivo")
                            .font(.omnes(13))
                            .foregroundStyle(secondaryText)
                    }

                    Spacer()

                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(secondaryText)
                }
                .frame(height: 56)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
    }

    // MARK: - Components

    private func card(
        title: String,
        @ViewBuilder content: () -> some View
    ) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.omnes(15, weight: .semiBold))
                .foregroundStyle(primaryText.opacity(0.85))

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

    private func settingsRow(
        icon: String,
        iconTint: Color,
        title: String,
        subtitle: String
    ) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(iconTint)
                .frame(width: 28)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.omnes(16, weight: .semiBold))
                    .foregroundStyle(primaryText)

                Text(subtitle)
                    .font(.omnes(13))
                    .foregroundStyle(secondaryText)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(secondaryText)
        }
        .frame(height: 56)
        .contentShape(Rectangle())
    }
}

private struct KeyValueRow: View {
    let title: String
    let value: String
    let primaryText: Color
    let secondaryText: Color
    let valueFont: Font

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.omnes(12))
                .foregroundStyle(secondaryText)

            Text(value)
                .font(valueFont)
                .foregroundStyle(primaryText)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)
        }
    }
}
