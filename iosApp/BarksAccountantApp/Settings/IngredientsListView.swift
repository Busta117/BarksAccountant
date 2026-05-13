import SwiftUI
import Shared

enum IngredientDestination: Hashable {
    case new
    case edit(String)
}

struct IngredientsListView: View {
    let serviceLocator: ServiceLocator
    @State private var store: IngredientsListStoreWrapper

    @Environment(\.colorScheme) private var colorScheme

    init(serviceLocator: ServiceLocator) {
        self.serviceLocator = serviceLocator
        _store = State(initialValue: IngredientsListStoreWrapper(
            ingredientRepository: serviceLocator.ingredientRepository
        ))
    }

    // MARK: - Theme

    private var screenBackground: Color {
        colorScheme == .dark ? .barksBlack : .barksWhite
    }

    private var cardBackground: Color {
        if colorScheme == .dark {
            return Color.white.opacity(0.06)
        } else {
            return Color.barksLightBlue.opacity(0.25)
        }
    }

    private var primaryText: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            screenBackground
                .ignoresSafeArea()

            content
                .navigationTitle("Ingredientes")
                .onAppear { store.start() }

            fabAddButton
        }
        .navigationDestination(for: IngredientDestination.self) { destination in
            switch destination {
            case .new:
                IngredientFormView(
                    serviceLocator: serviceLocator,
                    ingredientId: nil,
                    onSaved: { store.reload() }
                )
            case .edit(let ingredientId):
                IngredientFormView(
                    serviceLocator: serviceLocator,
                    ingredientId: ingredientId,
                    onSaved: { store.reload() }
                )
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        Group {
            if store.isLoading && store.ingredients.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if let error = store.error, store.ingredients.isEmpty {
                VStack(spacing: 12) {
                    Text(error)
                        .font(.omnes(17))
                        .foregroundStyle(primaryText.opacity(0.7))

                    Button("Reintentar") { store.reload() }
                        .buttonStyle(.borderedProminent)
                        .tint(.barksRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.horizontal, 24)

            } else if store.ingredients.isEmpty {
                Text("No hay ingredientes")
                    .font(.vagRundschrift(20))
                    .foregroundStyle(primaryText.opacity(0.7))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else {
                List {
                    ForEach(store.ingredients, id: \.id) { ingredient in
                        NavigationLink(value: IngredientDestination.edit(ingredient.id)) {
                            IngredientCardRow(
                                name: ingredient.name,
                                unitSymbol: ingredient.unit.symbol,
                                cardBackground: cardBackground
                            )
                        }
                        .buttonStyle(.plain)
                        .listRowSeparator(.hidden)
                        .listRowInsets(.init(top: 8, leading: 16, bottom: 8, trailing: 16))
                        .listRowBackground(Color.clear)
                    }
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
    }

    private var fabAddButton: some View {
        NavigationLink(value: IngredientDestination.new) {
            Image(systemName: "plus")
                .font(.system(size: 20, weight: .bold))
                .foregroundStyle(Color.barksWhite)
                .frame(width: 56, height: 56)
                .background(Color.barksRed)
                .clipShape(Circle())
                .shadow(
                    color: .black.opacity(colorScheme == .dark ? 0.35 : 0.18),
                    radius: 10,
                    x: 0,
                    y: 6
                )
                .accessibilityLabel("Add ingredient")
        }
        .padding(.trailing, 20)
        .padding(.bottom, 20)
    }
}

private struct IngredientCardRow: View {
    let name: String
    let unitSymbol: String
    let cardBackground: Color

    @Environment(\.colorScheme) private var colorScheme

    private var titleColor: Color {
        colorScheme == .dark ? .barksWhite : .barksBlack
    }

    private var secondaryColor: Color {
        titleColor.opacity(colorScheme == .dark ? 0.60 : 0.65)
    }

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            Text(name)
                .font(.omnes(17, weight: .semiBold))
                .foregroundStyle(titleColor)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)
                .layoutPriority(1)

            Spacer(minLength: 12)

            Text(unitSymbol)
                .font(.omnes(13, weight: .semiBold))
                .foregroundStyle(secondaryColor)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .fill(Color.gray.opacity(0.18))
                )
        }
        .padding(.vertical, 14)
        .padding(.horizontal, 16)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(cardBackground)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(Color.white.opacity(colorScheme == .dark ? 0.06 : 0.0), lineWidth: 1)
        )
        .shadow(
            color: .black.opacity(colorScheme == .dark ? 0.22 : 0.08),
            radius: 16,
            x: 0,
            y: 10
        )
    }
}
