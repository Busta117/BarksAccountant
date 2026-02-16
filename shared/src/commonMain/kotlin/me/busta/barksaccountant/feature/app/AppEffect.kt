package me.busta.barksaccountant.feature.app

sealed interface AppEffect {
    data object CheckStoredUser : AppEffect
    data class SaveUserId(val userId: String) : AppEffect
    data object ClearUserId : AppEffect
}
