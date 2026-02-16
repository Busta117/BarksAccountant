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

    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 4) {
                    Text("App ID")
                        .font(.omnes(12))
                        .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                    Text(serviceLocator.appId)
                        .font(.vagRundschrift(22))
                        .foregroundStyle(Color.barksPrincipal)
                    Text("Usuario")
                        .font(.omnes(12))
                        .foregroundStyle(Color.barksPrincipal.opacity(0.6))
                        .padding(.top, 4)
                    Text(personName)
                        .font(.omnes(17))
                        .foregroundStyle(Color.barksPrincipal)
                }
            }

            Section {
                NavigationLink(value: SettingsDestination.products) {
                    Label("Productos", systemImage: "tag.fill")
                }
                NavigationLink(value: SettingsDestination.clients) {
                    Label("Clientes", systemImage: "person.2.fill")
                }
            }

            Section {
                Button {
                    showLogoutConfirm = true
                } label: {
                    Label("Cerrar sesión", systemImage: "rectangle.portrait.and.arrow.right")
                }
                .buttonStyle(.barksDestructive)
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
}
