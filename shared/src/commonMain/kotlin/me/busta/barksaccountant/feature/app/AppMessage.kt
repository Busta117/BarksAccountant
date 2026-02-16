package me.busta.barksaccountant.feature.app

sealed interface AppMessage {
    data object CheckAuth : AppMessage
    data class AuthChecked(val userId: String?) : AppMessage
    data class LoggedIn(val userId: String) : AppMessage
    data object LoggedOut : AppMessage
}
