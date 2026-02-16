package me.busta.barksaccountant.feature.login

import me.busta.barksaccountant.data.repository.UserRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class LoginStore(
    private val userRepository: UserRepository,
    initialUserId: String = ""
) : Store<LoginState, LoginMessage, LoginEffect>(LoginState(userId = initialUserId)) {

    override fun reduce(state: LoginState, message: LoginMessage): Next<LoginState, LoginEffect> {
        return when (message) {
            is LoginMessage.UserIdChanged -> Next.just(
                state.copy(userId = message.text, error = null)
            )
            is LoginMessage.LoginTapped -> {
                if (state.userId.isBlank()) {
                    Next.just(state)
                } else {
                    Next.withEffects(
                        state.copy(isLoading = true, error = null),
                        LoginEffect.ValidateUser(state.userId)
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
            is LoginEffect.ValidateUser -> {
                try {
                    val isValid = userRepository.validateUser(effect.userId)
                    if (isValid) {
                        dispatch(LoginMessage.LoginSuccess(effect.userId))
                    } else {
                        dispatch(LoginMessage.LoginFailed("El usuario no existe"))
                    }
                } catch (e: Exception) {
                    dispatch(LoginMessage.LoginFailed(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
