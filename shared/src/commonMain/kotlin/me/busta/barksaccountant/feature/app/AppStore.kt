package me.busta.barksaccountant.feature.app

import me.busta.barksaccountant.data.LocalStorage
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class AppStore(
    private val localStorage: LocalStorage
) : Store<AppState, AppMessage, AppEffect>(AppState()) {

    companion object {
        private const val APP_ID_KEY = "app_id"
        private const val PERSON_NAME_KEY = "person_name"
    }

    override fun reduce(state: AppState, message: AppMessage): Next<AppState, AppEffect> {
        return when (message) {
            is AppMessage.CheckAuth -> Next.withEffects(
                state.copy(isCheckingAuth = true),
                AppEffect.CheckStoredAuth
            )
            is AppMessage.AuthChecked -> {
                val appId = message.appId
                val personName = message.personName
                if (appId != null && personName != null) {
                    Next.just(state.copy(
                        isLoggedIn = true,
                        appId = appId,
                        personName = personName,
                        isCheckingAuth = false
                    ))
                } else {
                    Next.just(state.copy(
                        isLoggedIn = false,
                        appId = null,
                        personName = null,
                        isCheckingAuth = false
                    ))
                }
            }
            is AppMessage.LoggedIn -> Next.withEffects(
                state.copy(
                    isLoggedIn = true,
                    appId = message.appId,
                    personName = message.personName,
                    isCheckingAuth = false
                ),
                AppEffect.SaveAuth(message.appId, message.personName)
            )
            is AppMessage.LoggedOut -> Next.withEffects(
                state.copy(
                    isLoggedIn = false,
                    appId = null,
                    personName = null,
                    isCheckingAuth = false
                ),
                AppEffect.ClearAuth
            )
        }
    }

    override suspend fun handleEffect(effect: AppEffect) {
        when (effect) {
            is AppEffect.CheckStoredAuth -> {
                val storedAppId = localStorage.getString(APP_ID_KEY)
                val storedPersonName = localStorage.getString(PERSON_NAME_KEY)
                dispatch(AppMessage.AuthChecked(storedAppId, storedPersonName))
            }
            is AppEffect.SaveAuth -> {
                localStorage.putString(APP_ID_KEY, effect.appId)
                localStorage.putString(PERSON_NAME_KEY, effect.personName)
            }
            is AppEffect.ClearAuth -> {
                localStorage.remove(APP_ID_KEY)
                localStorage.remove(PERSON_NAME_KEY)
            }
        }
    }
}
