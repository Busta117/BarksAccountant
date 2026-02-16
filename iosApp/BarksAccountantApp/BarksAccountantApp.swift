import SwiftUI
import Shared
import FirebaseCore
import FirebaseAnalytics

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        Analytics.setAnalyticsCollectionEnabled(true)
        return true
    }
}

private let firestoreBridge = FirestoreBridge()
let serviceLocator = ServiceLocator(
    localStorage: IosLocalStorage(),
    firestoreService: IosFirestoreService(bridge: firestoreBridge)
)

@main
struct BarksAccountantApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @State private var appStore = AppStoreWrapper(serviceLocator: serviceLocator)

    var body: some Scene {
        WindowGroup {
            Group {
                if appStore.isCheckingAuth {
                    ProgressView()
                } else if appStore.isLoggedIn {
                    MainTabView(serviceLocator: serviceLocator, personName: appStore.personName ?? "", onLogout: {
                        appStore.onLoggedOut()
                    })
                } else {
                    LoginView(serviceLocator: serviceLocator) { appId, personName in
                        serviceLocator.appId = appId
                        appStore.onLoggedIn(appId: appId, personName: personName)
                    }
                }
            }
            .onAppear { appStore.start() }
            .onChange(of: appStore.appId) { _, newAppId in
                if let appId = newAppId {
                    serviceLocator.appId = appId
                }
            }
        }
    }
}
