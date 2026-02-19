package me.busta.barksaccountant.android.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.R
import me.busta.barksaccountant.android.ui.theme.BarksBlack
import me.busta.barksaccountant.android.ui.theme.BarksPink
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.android.ui.theme.vagRundschriftStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.login.LoginMessage
import me.busta.barksaccountant.feature.login.LoginStore

@Composable
fun LoginScreen(
    serviceLocator: ServiceLocator,
    onLoginSuccess: (appId: String, personName: String) -> Unit
) {
    val store = remember {
        LoginStore(appIdRepository = serviceLocator.appIdRepository)
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    DisposableEffect(Unit) {
        onDispose { store.dispose() }
    }

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            onLoginSuccess(state.appId, state.personName)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.logo_notext_pink),
                contentDescription = "Barks Logo",
                modifier = Modifier
                    .width(200.dp)
                    .padding(bottom = 36.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CustomTextField(
                    value = state.appId,
                    onValueChange = { store.dispatch(LoginMessage.AppIdChanged(it)) },
                    label = "App ID",
                    colors = colors
                )

                CustomTextField(
                    value = state.personName,
                    onValueChange = { store.dispatch(LoginMessage.PersonNameChanged(it)) },
                    label = "Nombre",
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            val isEnabled = state.appId.trim().isNotEmpty() &&
                    state.personName.trim().isNotEmpty() &&
                    !state.isLoading

            Button(
                onClick = { store.dispatch(LoginMessage.LoginTapped) },
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .alpha(if (!isEnabled) 0.5f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BarksPink,
                    contentColor = Color.White,
                    disabledContainerColor = BarksPink,
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Login",
                        style = omnesStyle(17, androidx.compose.ui.text.font.FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(2f))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Natural bites for dogs",
                style = omnesStyle(13),
                color = colors.primaryText.copy(alpha = 0.3f)
            )
        }

        AnimatedVisibility(
            visible = state.error != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = 60.dp)
        ) {
            state.error?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BarksRed, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = error,
                        style = omnesStyle(14),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = omnesStyle(14, androidx.compose.ui.text.font.FontWeight.Medium),
            color = colors.primaryText
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = colors.primaryText.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = omnesStyle(16).copy(color = BarksBlack),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            ) { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        style = omnesStyle(16),
                        color = BarksBlack.copy(alpha = 0.3f)
                    )
                }
                innerTextField()
            }
        }
    }
}
