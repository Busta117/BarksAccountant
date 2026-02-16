package me.busta.barksaccountant.feature.login

sealed interface LoginEffect {
    data class ValidateAppId(val appId: String, val personName: String) : LoginEffect
}
