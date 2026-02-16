package me.busta.barksaccountant.feature.login

sealed interface LoginMessage {
    data class AppIdChanged(val text: String) : LoginMessage
    data class PersonNameChanged(val text: String) : LoginMessage
    data object LoginTapped : LoginMessage
    data class LoginSuccess(val appId: String, val personName: String) : LoginMessage
    data class LoginFailed(val error: String) : LoginMessage
}
