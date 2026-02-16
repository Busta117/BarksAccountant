package me.busta.barksaccountant.feature.login

data class LoginState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)
