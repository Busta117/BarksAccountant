package me.busta.barksaccountant.feature.login

import me.busta.barksaccountant.data.repository.AppIdRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class LoginStore(
    private val appIdRepository: AppIdRepository
) : Store<LoginState, LoginMessage, LoginEffect>(LoginState()) {

    override fun reduce(state: LoginState, message: LoginMessage): Next<LoginState, LoginEffect> {
        return when (message) {
            is LoginMessage.AppIdChanged -> Next.just(
                state.copy(appId = message.text, error = null)
            )
            is LoginMessage.PersonNameChanged -> Next.just(
                state.copy(personName = message.text, error = null)
            )
            is LoginMessage.LoginTapped -> {
                if (state.appId.isBlank() || state.personName.isBlank()) {
                    Next.just(state)
                } else {
                    Next.withEffects(
                        state.copy(isLoading = true, error = null),
                        LoginEffect.ValidateAppId(state.appId, state.personName)
                    )
                }
            }
            is LoginMessage.LoginSuccess -> Next.just(
                state.copy(isLoading = false, loginSuccess = true, error = null)
            )
            is LoginMessage.LoginFailed -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: LoginEffect) {
        when (effect) {
            is LoginEffect.ValidateAppId -> {
                try {
                    val isValid = appIdRepository.validateAppId(effect.appId)
                    if (isValid) {
                        dispatch(LoginMessage.LoginSuccess(effect.appId, effect.personName))
                    } else {
                        dispatch(LoginMessage.LoginFailed("El ID de app no existe"))
                    }
                } catch (e: Exception) {
                    dispatch(LoginMessage.LoginFailed(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
