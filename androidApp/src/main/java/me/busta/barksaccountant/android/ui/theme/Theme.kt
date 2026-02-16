package me.busta.barksaccountant.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BarksLightBlue,
    onPrimary = BarksBlack,
    primaryContainer = BarksLightBlue,
    onPrimaryContainer = BarksBlack,
    secondary = BarksPink,
    onSecondary = BarksBlack,
    error = BarksRed,
    onError = Color.White,
    background = Color.White,
    onBackground = BarksPrincipal,
    surface = Color.White,
    onSurface = BarksPrincipal,
    onSurfaceVariant = BarksPrincipal.copy(alpha = 0.6f),
    surfaceVariant = Color(0xFFF5F5F5),
    outline = BarksPrincipal.copy(alpha = 0.3f),
)

private val DarkColorScheme = darkColorScheme(
    primary = BarksLightBlue,
    onPrimary = BarksBlack,
    primaryContainer = BarksLightBlue,
    onPrimaryContainer = BarksBlack,
    secondary = BarksPink,
    onSecondary = BarksBlack,
    error = BarksRed,
    onError = Color.White,
    background = Color(0xFF121212),
    onBackground = BarksPrincipalDark,
    surface = Color(0xFF121212),
    onSurface = BarksPrincipalDark,
    onSurfaceVariant = BarksPrincipalDark.copy(alpha = 0.6f),
    surfaceVariant = Color(0xFF2C2C2C),
    outline = BarksPrincipalDark.copy(alpha = 0.3f),
)

@Composable
fun BarksAccountantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BarksTypography,
        content = content
    )
}
