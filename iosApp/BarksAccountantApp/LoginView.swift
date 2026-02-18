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

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                Spacer()

                // Logo + Brand Name
                VStack(spacing: 0) {
                    Image("logo_notext_pink")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 200)
                }
                .padding(.bottom, 36)

                // Form Fields
                VStack(alignment: .leading, spacing: 20) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("App ID")
                            .font(.omnes(14, weight: .medium))
                            .foregroundStyle(Color.barksPrincipal)
                        TextField("App ID", text: Binding(
                            get: { store.appId },
                            set: { store.appIdChanged($0) }
                        ))
                        .font(.omnes(16))
                        .foregroundStyle(Color.barksBlack)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.white)
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.barksPrincipal.opacity(0.12), lineWidth: 1)
                        )
                        .autocapitalization(.none)
                        .autocorrectionDisabled()
                    }

                    VStack(alignment: .leading, spacing: 6) {
                        Text("Nombre")
                            .font(.omnes(14, weight: .medium))
                            .foregroundStyle(Color.barksPrincipal)
                        TextField("Nombre", text: Binding(
                            get: { store.personName },
                            set: { store.personNameChanged($0) }
                        ))
                        .font(.omnes(16))
                        .foregroundStyle(Color.barksBlack)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.white)
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.barksPrincipal.opacity(0.12), lineWidth: 1)
                        )
                    }
                }
                .padding(.horizontal, 40)

                // Login Button
                Button(action: { store.loginTapped() }) {
                    if store.isLoading {
                        ProgressView()
                            .tint(.white)
                            .frame(maxWidth: .infinity)
                    } else {
                        Text("Login")
                            .font(.omnes(17, weight: .semiBold))
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                    }
                }
                .padding(.vertical, 14)
                .background(Color.barksPink)
                .cornerRadius(14)
                .disabled(
                    store.appId.trimmingCharacters(in: .whitespaces).isEmpty ||
                    store.personName.trimmingCharacters(in: .whitespaces).isEmpty ||
                    store.isLoading
                )
                .opacity(
                    (store.appId.trimmingCharacters(in: .whitespaces).isEmpty ||
                     store.personName.trimmingCharacters(in: .whitespaces).isEmpty) ? 0.5 : 1.0
                )
                .padding(.horizontal, 40)
                .padding(.top, 28)

                Spacer()
                Spacer()

            }
        }
        .background(
            ZStack {
                Color.clear.appBackground()
                
                // Bottom Pattern + Tagline
                VStack(spacing: 8) {
                    Spacer()
                    Image("pattern_login")
                        .resizable()
                        .scaledToFit()
                        .padding(.bottom, 24)
                    
                    Text("Natural bites for dogs")
                        .font(.omnes(13))
                        .foregroundStyle(Color.barksPrincipal.opacity(0.3))
                }
                .padding(.bottom, 24)
            }
                .ignoresSafeArea()
            
        )
        .onAppear { store.start() }
        .onChange(of: store.loginSuccess) { _, success in
            if success {
                onLoginSuccess(store.appId, store.personName)
            }
        }
        .overlay(alignment: .bottom) {
            if let error = store.error {
                Text(error)
                    .font(.omnes(14))
                    .foregroundStyle(.white)
                    .padding()
                    .background(Color.barksRed.cornerRadius(8))
                    .padding(.horizontal)
                    .padding(.bottom, 60)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut, value: store.error)
    }
}

