package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.busta.barksaccountant.android.ui.theme.BarksBlack
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.BarksPink
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.OmnesFontFamily
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.di.ServiceLocator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    serviceLocator: ServiceLocator,
    personName: String,
    onLogout: () -> Unit,
    onProductsClick: () -> Unit,
    onClientsClick: () -> Unit,
    onBusinessInfoClick: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val colors = barksColors()
    val isDark = isSystemInDarkTheme()

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; onLogout() }) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.screenBackground)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Account Card
                BarksCard(
                    title = "Cuenta",
                    colors = colors
                ) {
                    KeyValueRow(
                        title = "App ID",
                        value = serviceLocator.appId,
                        colors = colors,
                        valueFont = TextStyle(
                            fontFamily = OmnesFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            fontFeatureSettings = "tnum"
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    HorizontalDivider(
                        color = colors.primaryText.copy(alpha = if (isDark) 0.25f else 0.18f)
                    )

                    Spacer(Modifier.height(10.dp))

                    KeyValueRow(
                        title = "Usuario",
                        value = personName,
                        colors = colors,
                        valueFont = TextStyle(
                            fontFamily = OmnesFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }

                // Management Card
                BarksCard(
                    title = "Gestión",
                    colors = colors
                ) {
                    SettingsRow(
                        icon = Icons.Default.Sell,
                        iconTint = BarksLightBlue,
                        title = "Productos",
                        subtitle = "Crea y edita tu catálogo",
                        colors = colors,
                        onClick = onProductsClick
                    )

                    HorizontalDivider(
                        color = colors.primaryText.copy(alpha = if (isDark) 0.25f else 0.18f)
                    )

                    SettingsRow(
                        icon = Icons.Default.People,
                        iconTint = BarksPink,
                        title = "Clientes",
                        subtitle = "Gestiona tus compradores",
                        colors = colors,
                        onClick = onClientsClick
                    )

                    HorizontalDivider(
                        color = colors.primaryText.copy(alpha = if (isDark) 0.25f else 0.18f)
                    )

                    SettingsRow(
                        icon = Icons.Default.Business,
                        iconTint = BarksRed,
                        title = "Datos del negocio",
                        subtitle = "Información para facturas",
                        colors = colors,
                        onClick = onBusinessInfoClick
                    )
                }

                // Session Card
                BarksCard(
                    title = "Sesión",
                    colors = colors
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showLogoutConfirm = true }
                            .padding(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = BarksRed,
                            modifier = Modifier.size(18.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Cerrar sesión",
                                style = TextStyle(
                                    fontFamily = OmnesFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = BarksRed
                            )

                            Text(
                                text = "Sal de tu cuenta en este dispositivo",
                                style = TextStyle(
                                    fontFamily = OmnesFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp
                                ),
                                color = colors.secondaryText
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.secondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyValueRow(
    title: String,
    value: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    valueFont: TextStyle
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = OmnesFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            color = colors.secondaryText
        )

        Text(
            text = value,
            style = valueFont,
            color = colors.primaryText
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = OmnesFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = colors.primaryText
            )

            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = OmnesFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = colors.secondaryText
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(14.dp)
        )
    }
}
