package me.busta.barksaccountant.store

data class Next<out State, out Effect>(
    val state: State,
    val effects: List<Effect> = emptyList()
) {
    companion object {
        fun <S, E> just(state: S): Next<S, E> = Next(state)

        fun <S, E> withEffects(state: S, vararg effects: E): Next<S, E> =
            Next(state, effects.toList())
    }
}
