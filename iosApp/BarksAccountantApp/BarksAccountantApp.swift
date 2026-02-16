import SwiftUI
import Shared

let serviceLocator = ServiceLocator(localStorage: IosLocalStorage())

@main
struct BarksAccountantApp: App {
    @State private var appStore = AppStoreWrapper(serviceLocator: serviceLocator)

    var body: some Scene {
        WindowGroup {
            Group {
                if appStore.isCheckingAuth {
                    ProgressView()
                } else if appStore.isLoggedIn {
                    MainTabView(serviceLocator: serviceLocator)
                } else {
                    LoginView(serviceLocator: serviceLocator, storedUserId: nil) { userId in
                        appStore.onLoggedIn(userId: userId)
                    }
                }
            }
            .onAppear { appStore.start() }
        }
    }
}
