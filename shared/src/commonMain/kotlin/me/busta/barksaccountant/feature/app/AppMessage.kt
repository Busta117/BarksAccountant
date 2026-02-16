package me.busta.barksaccountant.feature.app

sealed interface AppMessage {
    data object CheckAuth : AppMessage
    data class AuthChecked(val appId: String?, val personName: String?) : AppMessage
    data class LoggedIn(val appId: String, val personName: String) : AppMessage
    data object LoggedOut : AppMessage
}
