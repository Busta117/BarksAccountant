import SwiftUI
import Shared

struct LoginView: View {
    @State private var store: LoginStoreWrapper
    var onLoginSuccess: (String) -> Void

    init(serviceLocator: ServiceLocator, storedUserId: String?, onLoginSuccess: @escaping (String) -> Void) {
        _store = State(initialValue: LoginStoreWrapper(
            userRepository: serviceLocator.userRepository,
            initialUserId: storedUserId ?? ""
        ))
        self.onLoginSuccess = onLoginSuccess
    }

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Text("BarksAccountant")
                .font(.largeTitle)
                .fontWeight(.bold)

            TextField("User ID", text: Binding(
                get: { store.userId },
                set: { store.userIdChanged($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .autocapitalization(.none)
            .autocorrectionDisabled()
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
            .buttonStyle(.borderedProminent)
            .disabled(store.userId.trimmingCharacters(in: .whitespaces).isEmpty || store.isLoading)
            .padding(.horizontal, 40)

            Spacer()
        }
        .onAppear { store.start() }
        .onChange(of: store.loginSuccess) { _, success in
            if success {
                onLoginSuccess(store.userId)
            }
        }
        .overlay(alignment: .bottom) {
            if let error = store.error {
                Text(error)
                    .foregroundStyle(.white)
                    .padding()
                    .background(Color.red.cornerRadius(8))
                    .padding()
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut, value: store.error)
    }
}
