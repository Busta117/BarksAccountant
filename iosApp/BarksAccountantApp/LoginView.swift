import SwiftUI
import Shared

struct LoginView: View {
    @State private var store: LoginStoreWrapper
    var onLoginSuccess: (String, String) -> Void

    init(serviceLocator: ServiceLocator, onLoginSuccess: @escaping (String, String) -> Void) {
        _store = State(initialValue: LoginStoreWrapper(
            appIdRepository: serviceLocator.appIdRepository
        ))
        self.onLoginSuccess = onLoginSuccess
    }

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Text("BarksAccountant")
                .font(.vagRundschrift(34))
                .foregroundStyle(Color.barksPrincipal)

            TextField("App ID", text: Binding(
                get: { store.appId },
                set: { store.appIdChanged($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .autocapitalization(.none)
            .autocorrectionDisabled()
            .padding(.horizontal, 40)

            TextField("Nombre", text: Binding(
                get: { store.personName },
                set: { store.personNameChanged($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .padding(.horizontal, 40)

            Button(action: {
                store.loginTapped()
            }) {
                if store.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Login")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.barks)
            .disabled(
                store.appId.trimmingCharacters(in: .whitespaces).isEmpty ||
                store.personName.trimmingCharacters(in: .whitespaces).isEmpty ||
                store.isLoading
            )
            .padding(.horizontal, 40)

            Spacer()
        }
        .onAppear { store.start() }
        .onChange(of: store.loginSuccess) { _, success in
            if success {
                onLoginSuccess(store.appId, store.personName)
            }
        }
        .overlay(alignment: .bottom) {
            if let error = store.error {
                Text(error)
                    .foregroundStyle(.white)
                    .padding()
                    .background(Color.barksRed.cornerRadius(8))
                    .padding()
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut, value: store.error)
    }
}
