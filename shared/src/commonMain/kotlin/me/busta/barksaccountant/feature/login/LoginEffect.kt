package me.busta.barksaccountant.feature.login

sealed interface LoginEffect {
    data class ValidateUser(val userId: String) : LoginEffect
}
