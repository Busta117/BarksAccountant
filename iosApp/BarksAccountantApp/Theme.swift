import SwiftUI
import UIKit

// MARK: - Colors

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255.0
        let g = Double((int >> 8) & 0xFF) / 255.0
        let b = Double(int & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }

    static let barksPrincipal = Color(
        UIColor { traitCollection in
            if traitCollection.userInterfaceStyle == .dark {
                return UIColor(
                    red: CGFloat(0xF3) / 255.0,
                    green: CGFloat(0xF1) / 255.0,
                    blue: CGFloat(0xEC) / 255.0,
                    alpha: 1
                )
            } else {
                return UIColor(
                    red: CGFloat(0x11) / 255.0,
                    green: CGFloat(0x12) / 255.0,
                    blue: CGFloat(0x25) / 255.0,
                    alpha: 1
                )
            }
        }
    )

    static let barksRed = Color(hex: "ED6565")
    static let barksPink = Color(hex: "F9A09B")
    static let barksLightBlue = Color(hex: "C7D9E1")
    static let barksWhite = Color(hex: "F3F1EC")
    static let barksBlack = Color(hex: "111225")
}

// MARK: - Fonts

extension Font {
    static func vagRundschrift(_ size: CGFloat) -> Font {
        .custom("VAGRundschriftD", size: size)
    }

    static func omnes(_ size: CGFloat, weight: Font.OmnesWeight = .regular) -> Font {
        .custom(weight.fontName, size: size)
    }

    enum OmnesWeight {
        case light, regular, medium, semiBold, bold

        var fontName: String {
            switch self {
            case .light: return "Omnes-Light"
            case .regular: return "Omnes-Regular"
            case .medium: return "Omnes-Medium"
            case .semiBold: return "Omnes-SemiBold"
            case .bold: return "Omnes-Bold"
            }
        }
    }
}

// MARK: - Button Styles

struct BarksButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(17, weight: .semiBold))
            .foregroundStyle(Color.barksBlack)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(Color.barksLightBlue)
            .cornerRadius(10)
            .opacity(configuration.isPressed ? 0.7 : 1.0)
    }
}

struct BarksDestructiveButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.omnes(17, weight: .semiBold))
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(Color.barksRed)
            .cornerRadius(10)
            .opacity(configuration.isPressed ? 0.7 : 1.0)
    }
}

extension ButtonStyle where Self == BarksButtonStyle {
    static var barks: BarksButtonStyle { BarksButtonStyle() }
}

extension ButtonStyle where Self == BarksDestructiveButtonStyle {
    static var barksDestructive: BarksDestructiveButtonStyle { BarksDestructiveButtonStyle() }
}
