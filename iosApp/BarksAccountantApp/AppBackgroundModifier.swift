import SwiftUI

public struct AppBackground: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    public init() {}

    public func body(content: Content) -> some View {
        content
            .background(
                (colorScheme == .dark ? Color.barksBlack : Color.barksWhite)
            )
    }
}

public extension View {
    func appBackground() -> some View {
        self.modifier(AppBackground())
    }
}
