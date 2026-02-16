# BarksAccountant - Project Context

## Overview
Kotlin Multiplatform (KMP) app for Android and iOS. Shared business logic in Kotlin, 100% native UI (Jetpack Compose on Android, SwiftUI on iOS). App spec in `app_screen_description.md`.

## Architecture: Unidirectional Data Flow (UDF)

### Core Pattern
```
View → dispatch(Message) → Store → reduce(State, Message) → Next(State, Effects)
                                         ↑                          ↓
                                         └── dispatch(Message) ← handleEffect(Effect)
```

### Base Classes (`shared/.../store/`)
- **`Store<State, Message, Effect>`**: Abstract base. Holds `StateFlow<State>`, processes messages through `reduce()` (pure function), executes effects via `handleEffect()`.
- **`Next<State, Effect>`**: Result of `reduce()`. Contains new state + list of side effects. Use `Next.just(state)` for no effects, `Next.withEffects(state, effect1, effect2)` for effects.

### Rules
1. `reduce()` must be a **pure function** — no side effects, no I/O, no coroutines
2. All side effects go in `handleEffect()` — Firestore calls, navigation, analytics
3. `handleEffect()` dispatches new messages back to the store with results
4. **Messages describe what happened** (e.g., `SalesLoaded`), not what to do
5. **Effects describe what to execute** (e.g., `LoadSales`), triggered by reduce
6. State is always **immutable** — use `data class` and `copy()`

### Adding a New Feature/Screen
1. Create `feature/<name>/` package in `shared/src/commonMain/kotlin/me/busta/barksaccountant/`
2. Define: `<Name>State.kt`, `<Name>Message.kt`, `<Name>Effect.kt`, `<Name>Store.kt`
3. iOS: Create `<Name>View.swift` + `<Name>StoreWrapper.swift` in `iosApp/BarksAccountantApp/`
4. Add new Swift files to `iosApp/BarksAccountantApp.xcodeproj/project.pbxproj` (PBXBuildFile, PBXFileReference, PBXGroup children, PBXSourcesBuildPhase)
5. Android: Create Composable `<Name>Screen.kt` in `androidApp/.../ui/screen/` (or subfolder)
6. Add navigation route in `MainScreen.kt` NavHost

## Project Structure

```
BarksAccountant/
├── shared/                          # KMP shared module
│   ├── build.gradle.kts             # KMP config: commonMain, androidMain, iosMain
│   └── src/
│       ├── commonMain/kotlin/me/busta/barksaccountant/
│       │   ├── Platform.kt          # expect fun platformName()
│       │   ├── store/               # UDF base classes
│       │   │   ├── Store.kt         # Abstract Store<State, Message, Effect>
│       │   │   └── Next.kt          # Next<State, Effect> with just() and withEffects()
│       │   ├── model/               # Data models
│       │   │   ├── User.kt          # data class User(id)
│       │   │   ├── Product.kt       # data class Product(id, name, unitPrice)
│       │   │   ├── Client.kt        # data class Client(id, name, responsible?, nif?, address?)
│       │   │   ├── Sale.kt          # data class Sale(id, clientName, responsible?, orderDate, deliveryDate?, products, totalPrice, isPaid, isDelivered)
│       │   │   ├── SaleProduct.kt   # data class SaleProduct(productId, name, unitPrice, quantity) + computed totalPrice
│       │   │   └── Purchase.kt      # data class Purchase(id, title, description?, value, date)
│       │   ├── data/
│       │   │   ├── LocalStorage.kt  # interface: getString, putString, remove
│       │   │   ├── FirestoreService.kt # interface: getDocument, setDocument, deleteDocument
│       │   │   └── repository/
│       │   │       ├── AppIdRepository.kt         # interface: validateAppId(appId)
│       │   │       ├── FirestoreAppIdRepository.kt  # Firestore impl: checks app_ids/{appId}
│       │   │       ├── SaleRepository.kt         # interface: getSales, getSale, saveSale, updateSale, deleteSale
│       │   │       ├── FirestoreSaleRepository.kt   # Firestore impl: apps/{appId}/sales
│       │   │       ├── ProductRepository.kt      # interface: getProducts, getProduct, saveProduct, updateProduct, deleteProduct
│       │   │       ├── FirestoreProductRepository.kt # Firestore impl: apps/{appId}/products
│       │   │       ├── ClientRepository.kt       # interface: getClients, getClient, saveClient, updateClient, deleteClient
│       │   │       ├── FirestoreClientRepository.kt  # Firestore impl: apps/{appId}/clients
│       │   │       ├── PurchaseRepository.kt     # interface: getPurchases, getPurchase, savePurchase, updatePurchase, deletePurchase
│       │   │       ├── FirestorePurchaseRepository.kt # Firestore impl: apps/{appId}/purchases
│       │   │       ├── InMemoryUserRepository.kt # valid users: "admin", "user1", "busta"
│       │   │       ├── InMemorySaleRepository.kt # 3 sample sales
│       │   │       ├── InMemoryProductRepository.kt # 5 sample products
│       │   │       └── InMemoryClientRepository.kt  # 3 sample clients
│       │   ├── di/
│       │   │   └── ServiceLocator.kt # Simple DI: lazy repos + localStorage
│       │   └── feature/
│       │       ├── app/              # AppStore: auth root (CheckAuth, LoggedIn, LoggedOut)
│       │       │   ├── AppState.kt, AppMessage.kt, AppEffect.kt, AppStore.kt
│       │       ├── login/            # LoginStore: appId+personName text, validate, success/fail
│       │       │   ├── LoginState.kt, LoginMessage.kt, LoginEffect.kt, LoginStore.kt
│       │       ├── sales/
│       │       │   ├── list/         # SalesListStore: load sales list
│       │       │   │   ├── SalesListState.kt, SalesListMessage.kt, SalesListEffect.kt, SalesListStore.kt
│       │       │   ├── detail/       # SaleDetailStore: view detail, mark paid/delivered
│       │       │   │   ├── SaleDetailState.kt, SaleDetailMessage.kt, SaleDetailEffect.kt, SaleDetailStore.kt
│       │       │   └── form/         # SaleFormStore: create/edit, manage products, delete
│       │       │       ├── SaleFormState.kt, SaleFormMessage.kt, SaleFormEffect.kt, SaleFormStore.kt
│       │       ├── purchases/
│       │       │   ├── list/         # PurchasesListStore: load purchases list
│       │       │   │   ├── PurchasesListState.kt, PurchasesListMessage.kt, PurchasesListEffect.kt, PurchasesListStore.kt
│       │       │   └── form/         # PurchaseFormStore: create/edit/delete purchase
│       │       │       ├── PurchaseFormState.kt, PurchaseFormMessage.kt, PurchaseFormEffect.kt, PurchaseFormStore.kt
│       │       └── settings/
│       │           ├── products/
│       │           │   ├── list/     # ProductsListStore: load products list
│       │           │   │   ├── ProductsListState.kt, ProductsListMessage.kt, ProductsListEffect.kt, ProductsListStore.kt
│       │           │   └── form/     # ProductFormStore: create/edit/delete product
│       │           │       ├── ProductFormState.kt, ProductFormMessage.kt, ProductFormEffect.kt, ProductFormStore.kt
│       │           └── clients/
│       │               ├── list/     # ClientsListStore: load clients list
│       │               │   ├── ClientsListState.kt, ClientsListMessage.kt, ClientsListEffect.kt, ClientsListStore.kt
│       │               └── form/     # ClientFormStore: create/edit/delete client
│       │                   ├── ClientFormState.kt, ClientFormMessage.kt, ClientFormEffect.kt, ClientFormStore.kt
│       ├── androidMain/kotlin/me/busta/barksaccountant/
│       │   ├── Platform.android.kt
│       │   └── data/
│       │       ├── AndroidLocalStorage.kt    # SharedPreferences impl
│       │       └── AndroidFirestoreService.kt
│       └── iosMain/kotlin/me/busta/barksaccountant/
│           ├── Platform.ios.kt
│           ├── data/
│           │   ├── IosLocalStorage.kt        # NSUserDefaults impl
│           │   └── IosFirestoreService.kt    # Bridge pattern for Firebase iOS SDK
│           └── util/
│               └── FlowCollector.kt          # StateFlow→Swift callback adapter
├── androidApp/                      # Android app — Gradle module :androidApp
│   ├── build.gradle.kts
│   ├── google-services.json         # Firebase config
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/values/themes.xml    # Theme.BarksAccountant (NoActionBar for Compose)
│       └── java/me/busta/barksaccountant/android/
│           ├── BarksAccountantApp.kt # Application class: inits ServiceLocator
│           ├── MainActivity.kt       # Entry point: AppStore auth switch → LoginScreen or MainScreen
│           └── ui/
│               ├── theme/
│               │   └── Theme.kt     # Material3 theme with dynamic colors
│               └── screen/
│                   ├── LoginScreen.kt    # Login: OutlinedTextField + Button + error Snackbar
│                   ├── MainScreen.kt     # Scaffold + BottomNavigation + NavHost (3 tabs, all routes)
│                   ├── sales/
│                   │   ├── SalesListScreen.kt    # LazyColumn of sales, red bar for unpaid, empty state
│                   │   ├── SaleDetailScreen.kt   # Sale info + mark paid/delivered + AlertDialogs
│                   │   ├── SaleFormScreen.kt     # Form: client/dates/products/save/delete
│                   │   ├── ClientPickerDialog.kt # AlertDialog with client list
│                   │   └── ProductPickerDialog.kt # AlertDialog with product list
│                   ├── purchases/
│                   │   ├── PurchasesListScreen.kt # LazyColumn of purchases, empty state
│                   │   └── PurchaseFormScreen.kt  # Form: title/desc/value/date/save/delete
│                   └── settings/
│                       ├── SettingsScreen.kt     # App ID, person name, Products/Clients/Logout
│                       ├── ProductsListScreen.kt # LazyColumn of products, empty state
│                       ├── ProductFormScreen.kt  # Form: name/price/save/delete
│                       ├── ClientsListScreen.kt  # LazyColumn of clients, empty state
│                       └── ClientFormScreen.kt   # Form: name/responsible/nif/address/save/delete
├── iosApp/                          # iOS app (SwiftUI)
│   ├── BarksAccountantApp.xcodeproj/project.pbxproj
│   └── BarksAccountantApp/
│       ├── BarksAccountantApp.swift  # @main App: creates ServiceLocator, auth switch
│       ├── AppStoreWrapper.swift     # @Observable wrapper for AppStore
│       ├── LoginView.swift           # TextField userId + Login button + toast error
│       ├── LoginStoreWrapper.swift   # @Observable wrapper for LoginStore
│       ├── MainTabView.swift         # TabView: Ventas, Compras, Settings (with onLogout)
│       ├── FirestoreBridge.swift     # Implements FirestoreServiceBridge ObjC protocol (lazy db)
│       ├── Info.plist
│       ├── Assets.xcassets/
│       ├── Sales/
│       │   ├── SalesListView.swift         # List of sales, empty state, NavigationLink to detail/form
│       │   ├── SalesListStoreWrapper.swift
│       │   ├── SaleDetailView.swift        # Sale info + mark paid/delivered buttons + alerts
│       │   ├── SaleDetailStoreWrapper.swift
│       │   ├── SaleFormView.swift          # Form: client picker, dates, products +-quantity, save, delete
│       │   ├── SaleFormStoreWrapper.swift  # Includes delete methods
│       │   ├── ClientPickerSheet.swift     # Modal sheet to select client
│       │   └── ProductPickerSheet.swift    # Modal sheet to add product
│       ├── Purchases/
│       │   ├── PurchasesListView.swift     # List of purchases, empty state, swipe-to-edit
│       │   ├── PurchasesListStoreWrapper.swift
│       │   ├── PurchaseFormView.swift      # Form: title/desc/value/date, save, delete
│       │   └── PurchaseFormStoreWrapper.swift  # Uses purchaseDescription (Swift NSObject conflict)
│       └── Settings/
│           ├── SettingsView.swift          # App ID, person name, Products/Clients links, Logout
│           ├── ProductsListView.swift      # List of products, empty state
│           ├── ProductsListStoreWrapper.swift
│           ├── ProductFormView.swift       # Form: name/price, save, delete
│           ├── ProductFormStoreWrapper.swift
│           ├── ClientsListView.swift       # List of clients, empty state
│           ├── ClientsListStoreWrapper.swift
│           ├── ClientFormView.swift        # Form: name/responsible/nif/address, save, delete
│           └── ClientFormStoreWrapper.swift
├── gradle/
│   └── libs.versions.toml           # Version catalog
├── build.gradle.kts                 # Root: declares plugins
├── settings.gradle.kts              # Modules: :shared, :androidApp
├── gradle.properties                # JVM args, java.home, android flags
├── local.properties                 # sdk.dir for Android SDK
├── gradlew                          # Modified: auto-detects JAVA_HOME on macOS
└── app_screen_description.md        # Full app specification (all screens)
```

## Data Layer

### Repository Pattern
- Interfaces in `shared/.../data/repository/` — platform-agnostic
- Firestore implementations with prefix `Firestore` (e.g., `FirestoreSaleRepository`)
- InMemory implementations kept for reference/testing
- IDs generated with `kotlin.uuid.Uuid.random().toString()` (`@OptIn(ExperimentalUuidApi::class)`)
- All data scoped by App ID: Firestore path `apps/{appId}/collection`
- Repositories: `SaleRepository`, `ProductRepository`, `ClientRepository`, `PurchaseRepository`, `AppIdRepository`
- Product/Client repos include `deleteProduct`/`deleteClient` for settings management

### LocalStorage
- Interface in `shared/.../data/LocalStorage.kt`
- `IosLocalStorage` uses `NSUserDefaults` (in `iosMain`)
- `AndroidLocalStorage` uses `SharedPreferences` (in `androidMain`, needs Context)
- Used by `AppStore` to persist App ID (key `"app_id"`) and person name (key `"person_name"`)

### ServiceLocator
- Simple DI in `shared/.../di/ServiceLocator.kt`
- Constructor receives `LocalStorage` + `FirestoreService`
- Has mutable `appId: String` property (set after login, used by Firestore repos)
- Provides repo instances via `get()` (not `lazy`) to pick up current `appId`
- Available repos: `appIdRepository`, `saleRepository`, `productRepository`, `clientRepository`, `purchaseRepository`
- iOS init: `ServiceLocator(localStorage: IosLocalStorage(), firestoreService: IosFirestoreService(bridge:))` — global in BarksAccountantApp.swift
- Android init: `ServiceLocator(localStorage: AndroidLocalStorage(context), firestoreService: AndroidFirestoreService())` — in BarksAccountantApp.kt Application class

## Firebase Integration

### Pattern: Native Bridge (no CocoaPods, no external KMP Firebase libs)
- `FirestoreService` interface in `commonMain` — platform-agnostic
- **Android** (`androidMain`): `AndroidFirestoreService` uses `com.google.firebase:firebase-firestore-ktx` directly with `await()`
- **iOS** (`iosMain`): `IosFirestoreService` delegates to `FirestoreServiceBridge` (Kotlin interface exported as ObjC protocol). Swift app implements this protocol using Firebase iOS SDK (added via SPM). Uses `suspendCoroutine` to convert callbacks.

### Setup Required
- Android: Replace `androidApp/google-services.json` with real Firebase config
- iOS: Add `GoogleService-Info.plist` to Xcode project, add Firebase iOS SDK via SPM

## iOS - Kotlin Interop

### StateFlow Observation
`FlowCollector<T>` in `iosMain/util/` converts Kotlin `StateFlow` to Swift callbacks.
Each SwiftUI screen has a `*StoreWrapper` (`@Observable`) that uses `FlowCollector` to sync state.

### StoreWrapper Pattern (every screen follows this)
```swift
@Observable
final class XxxStoreWrapper {
    private(set) var someState: SomeType = defaultValue // published state properties
    private let store: XxxStore
    private var collector: FlowCollector<XxxState>?

    init(dependencies...) {
        self.store = XxxStore(dependencies...)
    }

    func start() {
        collector = FlowCollector<XxxState>(flow: store.state, callback: { [weak self] state in
            guard let self else { return }
            self.someState = state.someState
        })
        store.dispatch(message: XxxMessageStarted.shared)
    }

    func someAction() { store.dispatch(message: XxxMessageSomething(...)) }

    deinit { collector?.close(); store.dispose() }
}
```

### Kotlin to Swift Naming Conventions
- `sealed interface XMessage` members become top-level classes: `XMessageName` in Swift
- `data object Started : XMessage` → `XMessageStarted.shared` (singleton accessor)
- `data class Foo(val x: String) : XMessage` → `XMessageFoo(x: "value")`
- `data class Bar(val index: Int) : XMessage` → `XMessageBar(index: Int32(index))` (Kotlin Int → Swift Int32)
- `List<T>` from Kotlin → cast with `as? [T] ?? []` in Swift
- Nullable `String?` from Kotlin → Optional `String?` in Swift (works directly)

### View Pattern (every SwiftUI view follows this)
```swift
struct XxxView: View {
    let serviceLocator: ServiceLocator
    @State private var store: XxxStoreWrapper
    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: XxxStoreWrapper(dep: serviceLocator.depRepository))
    }
    var body: some View { /* ... */ }
        .onAppear { store.start() }
}
```

## Android - Compose Patterns

### Store Usage in Compose (no wrapper needed)
Android uses `collectAsState()` directly on Kotlin `StateFlow` — no wrapper class required.
```kotlin
@Composable
fun XxxScreen(serviceLocator: ServiceLocator, ...) {
    val store = remember { XxxStore(dep = serviceLocator.depRepository) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) {
        store.dispatch(XxxMessage.Started)
    }

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    // Use state directly in Compose UI
}
```

### Key Differences from iOS
- No StoreWrapper needed — `collectAsState()` bridges StateFlow to Compose state directly
- Store created with `remember { }` — recreated when composable re-enters composition (auto-refresh on navigation back)
- Store disposed with `DisposableEffect` → `onDispose { store.dispose() }`
- Messages dispatched directly: `store.dispatch(XxxMessage.SomeThing)` (Kotlin sealed interface, not ObjC classes)
- No type casting needed for lists — Kotlin types used directly

### Navigation Pattern (Android)
- Root: `MainActivity` creates `AppStore`, switches between `LoginScreen` and `MainScreen` based on `appState.isLoggedIn`
- `MainScreen` uses `Scaffold` + `NavigationBar` (bottom bar) + `NavHost`
- 3 tabs: Ventas (sales_list), Compras (purchases_list), Settings (settings)
- Sales routes: `sales_list` → `sale_detail/{saleId}` → `sale_form?saleId={saleId}`
- Purchases routes: `purchases_list` → `purchase_form?purchaseId={purchaseId}`
- Settings routes: `settings` → `products_list` → `product_form?productId={productId}`, `clients_list` → `client_form?clientId={clientId}`
- Tab switching uses `popUpTo` + `saveState`/`restoreState` for proper back stack
- Back navigation: system back button pops NavHost backstack automatically
- Data refresh on back: composable is recreated when re-entering composition, triggering fresh data load via `LaunchedEffect(Unit)`

### Screen Pattern (every Compose screen follows this)
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XxxScreen(serviceLocator: ServiceLocator, onBack: () -> Unit, ...) {
    val store = remember { XxxStore(...) }
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) { store.dispatch(XxxMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Title") }, navigationIcon = { ... }) }
    ) { padding ->
        // Content with Modifier.padding(padding)
    }
}
```

### Pickers / Dialogs (Android)
- Client/Product selection: `AlertDialog` with `LazyColumn` (vs iOS `sheet` with `List`)
- Date selection: Material3 `DatePickerDialog` + `DatePicker` (vs iOS `DatePicker`)
- Confirmation dialogs: `AlertDialog` (vs iOS `.alert()`)

## Navigation Pattern (iOS)
- Root: `BarksAccountantApp` switches between `LoginView` and `MainTabView` based on `appStore.isLoggedIn`
- Each tab has its own `NavigationStack`
- Push navigation via `NavigationLink(value:)` + `.navigationDestination(for:)`
- Modals via `.sheet(isPresented:)` for pickers (ClientPickerSheet, ProductPickerSheet)
- On save/update, callback triggers `store.reload()` on parent and `dismiss()`

## Xcode Project (project.pbxproj)
- DEVELOPMENT_TEAM = NUPFXBM695
- When adding new Swift files, must add entries to 4 sections: PBXBuildFile, PBXFileReference, PBXGroup (children), PBXSourcesBuildPhase (files)
- Use incremental IDs like A100XX/B100XX for new entries
- Sales/ is PBXGroup D10004, Purchases/ is D10005, Settings/ is D10006 — all under BarksAccountantApp group (D10002)
- File reference IDs: A10040-A10052 (new files), Build file IDs: B10040-B10052
- Build phase "Compile Kotlin Framework" runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`
- FRAMEWORK_SEARCH_PATHS: `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
- OTHER_LDFLAGS: `-framework Shared`

## Build Environment
- Java: OpenJDK 17 via Homebrew at `/opt/homebrew/opt/openjdk@17`
- `gradlew` modified to auto-detect JAVA_HOME on macOS (no manual export needed)
- `gradle.properties` has `org.gradle.java.home=/opt/homebrew/opt/openjdk@17`
- Xcode build phase also exports JAVA_HOME as fallback
- Android SDK at `/Users/khks381/Library/Android/sdk`
- Xcode 26.2 with iOS 26.2 simulators (iPhone 17 Pro, etc.)

## Tech Stack
- Kotlin: 2.0.21, AGP: 8.5.2, Gradle: 8.9
- KotlinX Coroutines: 1.8.1, KotlinX Datetime: 0.6.1
- Compose BOM: 2024.09.03, Navigation Compose: 2.7.7
- Material Icons Extended (for full icon set)
- Firebase Android BOM: 33.3.0
- Min Android SDK: 28, compileSdk: 36
- iOS Deployment Target: 17.0
- Package: me.busta.barksaccountant
- Version catalog: `gradle/libs.versions.toml`

## Build Commands
- Shared: `./gradlew :shared:build`
- Shared iOS only: `./gradlew :shared:compileKotlinIosSimulatorArm64`
- Android: `./gradlew :androidApp:assembleDebug`
- iOS: `cd iosApp && xcodebuild -project BarksAccountantApp.xcodeproj -scheme BarksAccountantApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.2' build`
- Or open `iosApp/BarksAccountantApp.xcodeproj` in Xcode

## Implementation Status

### Login (iOS + Android): COMPLETE
- iOS: Two TextFields (App ID + Nombre de persona) + Login button + toast error
- Android: Two OutlinedTextFields + Button + animated error Snackbar
- Validates App ID against Firestore `app_ids` collection
- Persists App ID and person name in LocalStorage for auto-login
- Root auth switch: iOS via `BarksAccountantApp.swift`, Android via `MainActivity.kt`

### Sales — Ventas (iOS + Android): COMPLETE
- **List**: shows client name, date, price; red left border for unpaid; + button for new; **empty state** "No hay ventas"
- **Detail**: all sale info, mark as paid (alert confirm), mark as delivered (alert confirm, sets deliveryDate to today), edit button pushes form
- **Form (create/edit)**: client picker (sheet on iOS, dialog on Android), responsible field, date pickers (order + optional delivery), products list with +/- quantity (- at 1 = delete/trash icon), add product picker, total, save button, **delete button with confirmation** (only in edit mode)

### Purchases — Compras (iOS + Android): COMPLETE
- **List**: shows title, description, date, value; swipe-to-edit on iOS; + button for new; **empty state** "No hay compras"
- **Form (create/edit)**: title (required), description (optional), value (required, numeric > 0), date (required, default today), save button, **delete button with confirmation** (only in edit mode)
- Shared stores: PurchasesListStore, PurchaseFormStore (with createdBy param)
- Note: `PurchaseFormState.value` is `String` for text field binding; validated in `canSave`

### Settings (iOS + Android): COMPLETE
- **Root**: displays App ID (large) and person name, links to Products and Clients, Logout with confirmation
- **Products list**: name + price, empty state "No hay productos", + button
- **Product form**: name + price, save, **delete with confirmation** (edit mode)
- **Clients list**: name + responsible, empty state "No hay clientes", + button
- **Client form**: name + responsible + nif + address, save, **delete with confirmation** (edit mode)
- **Logout**: confirmation alert → dispatches `AppMessage.LoggedOut` / `AppMessageLoggedOut.shared` → clears local storage → returns to login

### Firebase/Firestore Integration: COMPLETE
- All repositories use Firestore: `FirestoreSaleRepository`, `FirestoreProductRepository`, `FirestoreClientRepository`, `FirestorePurchaseRepository`, `FirestoreAppIdRepository`
- Data isolated by App ID under `apps/{appId}/` subcollections
- iOS: `FirestoreBridge.swift` implements `FirestoreServiceBridge` protocol using Firebase iOS SDK (SPM). `db` is lazy to avoid init before `FirebaseApp.configure()`. Uses `UIApplicationDelegateAdaptor` for proper init ordering.
- Android: `AndroidFirestoreService` uses `firebase-firestore-ktx` with `await()`. `FirebaseApp.initializeApp(this)` called in Application class.
- Firebase Analytics enabled on both platforms

### Pending Features
- Export sale detail (placeholder button exists)
- Any future enhancements per `app_screen_description.md`

## Known Issues / Notes
- `kotlin.uuid.Uuid` requires `@OptIn(ExperimentalUuidApi::class)` — used in InMemory repos
- `kotlinx.datetime` imports needed explicitly: `import kotlinx.datetime.Clock`, `import kotlinx.datetime.toLocalDateTime`
- Firebase BOM must be in `android { dependencies { } }` block in shared/build.gradle.kts, NOT in KMP sourceSets.androidMain.dependencies
- `android.suppressUnsupportedCompileSdk=36` in gradle.properties (AGP 8.5.2 not tested with SDK 36)
- `kotlin.apple.xcodeCompatibility.nowarn=true` suppresses Xcode version warning
- Android module is `:androidApp` in Gradle (directory `androidApp/`)
- Material3 DatePicker returns millis in UTC — format with `SimpleDateFormat("yyyy-MM-dd")` using UTC timezone
