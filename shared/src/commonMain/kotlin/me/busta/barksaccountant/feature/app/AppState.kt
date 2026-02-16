package me.busta.barksaccountant.feature.app

data class AppState(
    val isLoggedIn: Boolean = false,
    val appId: String? = null,
    val personName: String? = null,
    val isCheckingAuth: Boolean = true
)
