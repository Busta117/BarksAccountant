package me.busta.barksaccountant.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Adaptive theme colors ───────────────────────────────────────────────────

@Stable
data class BarksColors(
    val screenBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val fieldBackground: Color,
    val fieldBorder: Color,
    val accentColor: Color,
    val isDark: Boolean
)

@Composable
fun barksColors(): BarksColors {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        BarksColors(
            screenBackground = BarksBlack,
            cardBackground = Color.White.copy(alpha = 0.06f),
            cardBorder = Color.White.copy(alpha = 0.06f),
            primaryText = BarksWhite,
            secondaryText = BarksWhite.copy(alpha = 0.60f),
            fieldBackground = Color.White.copy(alpha = 0.05f),
            fieldBorder = Color.White.copy(alpha = 0.10f),
            accentColor = BarksLightBlue,
            isDark = true
        )
    } else {
        BarksColors(
            screenBackground = BarksWhite,
            cardBackground = BarksLightBlue.copy(alpha = 0.25f),
            cardBorder = Color.Transparent,
            primaryText = BarksBlack,
            secondaryText = BarksBlack.copy(alpha = 0.65f),
            fieldBackground = Color.White.copy(alpha = 0.7f),
            fieldBorder = Color.Black.copy(alpha = 0.06f),
            accentColor = Color(0xFF6899A8),
            isDark = false
        )
    }
}

// ─── Card composable ─────────────────────────────────────────────────────────

@Composable
fun BarksCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    colors: BarksColors = barksColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.18f else 0.08f),
                spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.18f else 0.08f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBackground)
            .then(
                if (colors.isDark) Modifier.border(
                    1.dp,
                    colors.cardBorder,
                    RoundedCornerShape(18.dp)
                ) else Modifier
            )
            .padding(16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = OmnesFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = colors.primaryText.copy(alpha = 0.85f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        content()
    }
}

// ─── FAB ─────────────────────────────────────────────────────────────────────

@Composable
fun BarksFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: BarksColors = barksColors()
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = BarksRed,
        contentColor = BarksWhite,
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add",
            modifier = Modifier.size(24.dp)
        )
    }
}

// ─── Text styles (matching iOS Omnes sizes) ──────────────────────────────────

fun omnesStyle(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = OmnesFontFamily,
    fontWeight = weight,
    fontSize = size.sp
)

fun vagRundschriftStyle(size: Int) = TextStyle(
    fontFamily = VagRundschrift,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp
)
