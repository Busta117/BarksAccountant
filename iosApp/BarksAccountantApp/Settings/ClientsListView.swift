import SwiftUI
import Shared

struct ClientsListView: View {
    let serviceLocator: ServiceLocator
    @State private var store: ClientsListStoreWrapper

    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: ClientsListStoreWrapper(
            clientRepository: serviceLocator.clientRepository
        ))
    }

    // MARK: - Theme

    private var screenBackground: Color {
        colorScheme == .dark ? .barksBlack : .barksWhite
    }

    private var cardBackground: Color {
        colorScheme == .dark
        ? Color.white.opacity(0.06)
        : Color.barksLightBlue.opacity(0.25)
    }

    private var primaryText: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    private var secondaryText: Color {
        primaryText.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            screenBackground
                .ignoresSafeArea()

            content
                .navigationTitle("Clientes")
                .onAppear { store.start() }

            fabAddButton
        }
        .navigationDestination(for: ClientDestination.self) { destination in
            switch destination {
            case .new:
                ClientFormView(
                    serviceLocator: serviceLocator,
                    clientId: nil,
                    onSaved: { store.reload() }
                )
            case .edit(let clientId):
                ClientFormView(
                    serviceLocator: serviceLocator,
                    clientId: clientId,
                    onSaved: { store.reload() }
                )
            }
        }
    }

    // MARK: - Content

    @ViewBuilder
    private var content: some View {
        if store.isLoading && store.clients.isEmpty {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)

        } else if let error = store.error, store.clients.isEmpty {
            VStack(spacing: 12) {
                Text(error)
                    .font(.omnes(17))
                    .foregroundColor(primaryText.opacity(0.7))

                Button("Reintentar") {
                    store.reload()
                }
                .buttonStyle(.borderedProminent)
                .tint(.barksRed)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.horizontal, 24)

        } else if store.clients.isEmpty {
            Text("No hay clientes")
                .font(.vagRundschrift(20))
                .foregroundColor(primaryText.opacity(0.7))
                .frame(maxWidth: .infinity, maxHeight: .infinity)

        } else {
            List {
                ForEach(store.clients, id: \.id) { client in
                    NavigationLink(value: ClientDestination.edit(client.id)) {
                        ClientCardRow(
                            name: client.name,
                            cardBackground: cardBackground
                        )
                    }
                    .buttonStyle(.plain)
                    .listRowSeparator(.hidden)
                    .listRowInsets(
                        EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16)
                    )
                    .listRowBackground(Color.clear)
                }
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
        }
    }

    // MARK: - Floating Button

    private var fabAddButton: some View {
        NavigationLink(value: ClientDestination.new) {
            Image(systemName: "plus")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.barksWhite)
                .frame(width: 56, height: 56)
                .background(Color.barksRed)
                .clipShape(Circle())
                .shadow(
                    color: .black.opacity(colorScheme == .dark ? 0.35 : 0.18),
                    radius: 10,
                    x: 0,
                    y: 6
                )
        }
        .padding(.trailing, 20)
        .padding(.bottom, 20)
    }
}

// MARK: - Client Card

private struct ClientCardRow: View {
    let name: String
    let cardBackground: Color

    @Environment(\.colorScheme) private var colorScheme

    private var titleColor: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    var body: some View {
        HStack {
            Text(name)
                .font(.omnes(17, weight: .semiBold))
                .foregroundColor(titleColor)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)

            Spacer()
        }
        .padding(.vertical, 14)
        .padding(.horizontal, 16)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(cardBackground)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(
                    Color.white.opacity(colorScheme == .dark ? 0.06 : 0.0),
                    lineWidth: 1
                )
        )
        .shadow(
            color: .black.opacity(colorScheme == .dark ? 0.22 : 0.08),
            radius: 16,
            x: 0,
            y: 10
        )
    }
}
