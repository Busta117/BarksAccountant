package me.busta.barksaccountant.feature.app

data class AppState(
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val isCheckingAuth: Boolean = true
)
