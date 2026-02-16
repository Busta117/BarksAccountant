package me.busta.barksaccountant.feature.login

data class LoginState(
    val appId: String = "",
    val personName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)
