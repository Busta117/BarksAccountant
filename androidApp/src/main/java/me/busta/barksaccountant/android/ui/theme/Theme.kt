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
    background = BarksWhite,
    onBackground = BarksPrincipal,
    surface = BarksWhite,
    onSurface = BarksPrincipal,
    onSurfaceVariant = BarksPrincipal.copy(alpha = 0.65f),
    surfaceVariant = BarksLightBlue.copy(alpha = 0.25f),
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
    background = BarksBlack,
    onBackground = BarksPrincipalDark,
    surface = BarksBlack,
    onSurface = BarksPrincipalDark,
    onSurfaceVariant = BarksPrincipalDark.copy(alpha = 0.60f),
    surfaceVariant = Color.White.copy(alpha = 0.06f),
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
