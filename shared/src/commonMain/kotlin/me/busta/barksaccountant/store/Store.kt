package me.busta.barksaccountant.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class Store<State : Any, Message : Any, Effect : Any>(
    initialState: State
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    protected val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun dispatch(message: Message) {
        val current = _state.value
        val next = reduce(current, message)
        _state.value = next.state
        next.effects.forEach { effect ->
            storeScope.launch { handleEffect(effect) }
        }
    }

    protected abstract fun reduce(state: State, message: Message): Next<State, Effect>

    protected abstract suspend fun handleEffect(effect: Effect)

    fun dispose() {
        storeScope.cancel()
    }
}
