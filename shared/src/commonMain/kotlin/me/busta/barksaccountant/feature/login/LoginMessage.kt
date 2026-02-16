package me.busta.barksaccountant.feature.login

sealed interface LoginMessage {
    data class UserIdChanged(val text: String) : LoginMessage
    data object LoginTapped : LoginMessage
    data class LoginSuccess(val userId: String) : LoginMessage
    data class LoginFailed(val error: String) : LoginMessage
}
