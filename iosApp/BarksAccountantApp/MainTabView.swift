import SwiftUI
import Shared

struct MainTabView: View {
    let serviceLocator: ServiceLocator
    let personName: String
    var onLogout: () -> Void

    var body: some View {
        TabView {
            SalesTab(serviceLocator: serviceLocator, personName: personName)
                .tabItem {
                    Label("Ventas", systemImage: "cart.fill")
                }

            PurchasesTab(serviceLocator: serviceLocator, personName: personName)
                .tabItem {
                    Label("Compras", systemImage: "bag.fill")
                }

            StatsTab(serviceLocator: serviceLocator)
                .tabItem {
                    Label("Stats", systemImage: "chart.bar.fill")
                }

            SettingsTab(serviceLocator: serviceLocator, personName: personName, onLogout: onLogout)
                .tabItem {
                    Label("Settings", systemImage: "gearshape.fill")
                }
        }
        .tint(Color.barksLightBlue)
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

struct PurchasesTab: View {
    let serviceLocator: ServiceLocator
    let personName: String

    var body: some View {
        NavigationStack {
            PurchasesListView(serviceLocator: serviceLocator, personName: personName)
        }
    }
}

struct StatsTab: View {
    let serviceLocator: ServiceLocator

    var body: some View {
        NavigationStack {
            StatsView(serviceLocator: serviceLocator)
        }
    }
}

struct SettingsTab: View {
    let serviceLocator: ServiceLocator
    let personName: String
    var onLogout: () -> Void

    var body: some View {
        NavigationStack {
            SettingsView(serviceLocator: serviceLocator, personName: personName, onLogout: onLogout)
        }
    }
}
