package me.busta.barksaccountant.feature.app

import me.busta.barksaccountant.data.LocalStorage
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class AppStore(
    private val localStorage: LocalStorage
) : Store<AppState, AppMessage, AppEffect>(AppState()) {

    companion object {
        private const val USER_ID_KEY = "user_id"
    }

    override fun reduce(state: AppState, message: AppMessage): Next<AppState, AppEffect> {
        return when (message) {
            is AppMessage.CheckAuth -> Next.withEffects(
                state.copy(isCheckingAuth = true),
                AppEffect.CheckStoredUser
            )
            is AppMessage.AuthChecked -> {
                val userId = message.userId
                if (userId != null) {
                    Next.just(state.copy(isLoggedIn = true, userId = userId, isCheckingAuth = false))
                } else {
                    Next.just(state.copy(isLoggedIn = false, userId = null, isCheckingAuth = false))
                }
            }
            is AppMessage.LoggedIn -> Next.withEffects(
                state.copy(isLoggedIn = true, userId = message.userId, isCheckingAuth = false),
                AppEffect.SaveUserId(message.userId)
            )
            is AppMessage.LoggedOut -> Next.withEffects(
                state.copy(isLoggedIn = false, userId = null, isCheckingAuth = false),
                AppEffect.ClearUserId
            )
        }
    }

    override suspend fun handleEffect(effect: AppEffect) {
        when (effect) {
            is AppEffect.CheckStoredUser -> {
                val storedUserId = localStorage.getString(USER_ID_KEY)
                dispatch(AppMessage.AuthChecked(storedUserId))
            }
            is AppEffect.SaveUserId -> {
                localStorage.putString(USER_ID_KEY, effect.userId)
            }
            is AppEffect.ClearUserId -> {
                localStorage.remove(USER_ID_KEY)
            }
        }
    }
}
