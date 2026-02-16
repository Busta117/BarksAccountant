import SwiftUI
import Shared

struct MainTabView: View {
    let serviceLocator: ServiceLocator
    let personName: String

    var body: some View {
        TabView {
            SalesTab(serviceLocator: serviceLocator, personName: personName)
                .tabItem {
                    Label("Ventas", systemImage: "cart.fill")
                }

            NavigationStack {
                Text("Compras - Próximamente")
                    .font(.title2)
                    .foregroundStyle(.secondary)
                    .navigationTitle("Compras")
            }
            .tabItem {
                Label("Compras", systemImage: "bag.fill")
            }

            NavigationStack {
                Text("Settings - Próximamente")
                    .font(.title2)
                    .foregroundStyle(.secondary)
                    .navigationTitle("Settings")
            }
            .tabItem {
                Label("Settings", systemImage: "gearshape.fill")
            }
        }
    }
}

struct SalesTab: View {
    let serviceLocator: ServiceLocator
    let personName: String

    var body: some View {
        NavigationStack {
            SalesListView(serviceLocator: serviceLocator, personName: personName)
        }
    }
}
