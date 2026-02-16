package me.busta.barksaccountant.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlowCollector<T : Any>(
    private val flow: StateFlow<T>,
    private val callback: (T) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            flow.collect { value ->
                callback(value)
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
