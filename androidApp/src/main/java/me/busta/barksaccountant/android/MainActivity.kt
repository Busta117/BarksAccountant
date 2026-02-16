package me.busta.barksaccountant.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.busta.barksaccountant.android.ui.screen.LoginScreen
import me.busta.barksaccountant.android.ui.screen.MainScreen
import me.busta.barksaccountant.android.ui.theme.BarksAccountantTheme
import me.busta.barksaccountant.feature.app.AppMessage
import me.busta.barksaccountant.feature.app.AppStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as BarksAccountantApp
        val serviceLocator = app.serviceLocator

        setContent {
            BarksAccountantTheme {
                val appStore = remember { AppStore(localStorage = serviceLocator.localStorage) }
                val appState by appStore.state.collectAsState()

                LaunchedEffect(Unit) {
                    appStore.dispatch(AppMessage.CheckAuth)
                }

                DisposableEffect(Unit) {
                    onDispose { appStore.dispose() }
                }

                when {
                    appState.isCheckingAuth -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    appState.isLoggedIn -> {
                        MainScreen(
                            serviceLocator = serviceLocator,
                            onLogout = { appStore.dispatch(AppMessage.LoggedOut) }
                        )
                    }
                    else -> {
                        LoginScreen(
                            serviceLocator = serviceLocator,
                            onLoginSuccess = { userId ->
                                appStore.dispatch(AppMessage.LoggedIn(userId))
                            }
                        )
                    }
                }
            }
        }
    }
}
