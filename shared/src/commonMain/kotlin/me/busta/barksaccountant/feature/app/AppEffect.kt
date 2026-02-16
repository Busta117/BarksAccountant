package me.busta.barksaccountant.feature.app

sealed interface AppEffect {
    data object CheckStoredAuth : AppEffect
    data class SaveAuth(val appId: String, val personName: String) : AppEffect
    data object ClearAuth : AppEffect
}
