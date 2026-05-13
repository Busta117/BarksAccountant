package me.busta.barksaccountant.feature.settings.ingredients.list

import me.busta.barksaccountant.data.repository.IngredientRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class IngredientsListStore(
    private val ingredientRepository: IngredientRepository
) : Store<IngredientsListState, IngredientsListMessage, IngredientsListEffect>(IngredientsListState()) {

    override fun reduce(
        state: IngredientsListState,
        message: IngredientsListMessage
    ): Next<IngredientsListState, IngredientsListEffect> {
        return when (message) {
            is IngredientsListMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                IngredientsListEffect.LoadIngredients
            )
            is IngredientsListMessage.IngredientsLoaded -> Next.just(
                state.copy(ingredients = message.ingredients, isLoading = false, error = null)
            )
            is IngredientsListMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: IngredientsListEffect) {
        when (effect) {
            is IngredientsListEffect.LoadIngredients -> {
                try {
                    val list = ingredientRepository.getIngredients()
                    dispatch(IngredientsListMessage.IngredientsLoaded(list))
                } catch (e: Exception) {
                    dispatch(IngredientsListMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
