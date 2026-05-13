package me.busta.barksaccountant.feature.settings.ingredients.form

import me.busta.barksaccountant.data.repository.IngredientRepository
import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.model.Ingredient
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class IngredientFormStore(
    private val ingredientRepository: IngredientRepository,
    private val productRepository: ProductRepository
) : Store<IngredientFormState, IngredientFormMessage, IngredientFormEffect>(IngredientFormState()) {

    override fun reduce(
        state: IngredientFormState,
        message: IngredientFormMessage
    ): Next<IngredientFormState, IngredientFormEffect> {
        return when (message) {
            is IngredientFormMessage.Started -> Next.withEffects(
                state.copy(ingredientId = message.ingredientId, isEditing = message.ingredientId != null),
                IngredientFormEffect.LoadIngredient(message.ingredientId)
            )
            is IngredientFormMessage.IngredientLoaded -> {
                val i = message.ingredient ?: return Next.just(state)
                Next.just(state.copy(name = i.name, unit = i.unit))
            }
            is IngredientFormMessage.NameChanged -> Next.just(state.copy(name = message.text))
            is IngredientFormMessage.UnitChanged -> {
                if (state.isEditing) Next.just(state) else Next.just(state.copy(unit = message.unit))
            }
            is IngredientFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val ingredient = Ingredient(
                    id = state.ingredientId ?: Uuid.random().toString(),
                    name = state.name.trim(),
                    unit = state.unit
                )
                if (state.isEditing) {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        IngredientFormEffect.UpdateIngredient(ingredient)
                    )
                } else {
                    Next.withEffects(
                        state.copy(isSaving = true, error = null),
                        IngredientFormEffect.SaveIngredient(ingredient)
                    )
                }
            }
            is IngredientFormMessage.SaveSuccess -> Next.just(state.copy(isSaving = false, savedSuccessfully = true))
            is IngredientFormMessage.DeleteTapped -> Next.just(state.copy(showDeleteConfirm = true))
            is IngredientFormMessage.ConfirmDelete -> {
                val id = state.ingredientId ?: return Next.just(state)
                Next.withEffects(
                    state.copy(showDeleteConfirm = false, isSaving = true),
                    IngredientFormEffect.DeleteIngredient(id)
                )
            }
            is IngredientFormMessage.DismissDelete -> Next.just(state.copy(showDeleteConfirm = false))
            is IngredientFormMessage.DismissDeleteBlocked -> Next.just(state.copy(deleteBlockedBy = emptyList()))
            is IngredientFormMessage.DeleteBlockedBy -> Next.just(
                state.copy(isSaving = false, deleteBlockedBy = message.productNames)
            )
            is IngredientFormMessage.DeleteSuccess -> Next.just(state.copy(isSaving = false, deletedSuccessfully = true))
            is IngredientFormMessage.ErrorOccurred -> Next.just(state.copy(isSaving = false, error = message.error))
        }
    }

    override suspend fun handleEffect(effect: IngredientFormEffect) {
        when (effect) {
            is IngredientFormEffect.LoadIngredient -> {
                try {
                    val i = effect.ingredientId?.let { ingredientRepository.getIngredient(it) }
                    dispatch(IngredientFormMessage.IngredientLoaded(i))
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is IngredientFormEffect.SaveIngredient -> {
                try {
                    ingredientRepository.saveIngredient(effect.ingredient)
                    dispatch(IngredientFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
            is IngredientFormEffect.UpdateIngredient -> {
                try {
                    ingredientRepository.updateIngredient(effect.ingredient)
                    dispatch(IngredientFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error al actualizar"))
                }
            }
            is IngredientFormEffect.DeleteIngredient -> {
                try {
                    val products = productRepository.getProducts()
                    val usedIn = products
                        .filter { p -> p.ingredients.any { it.ingredientId == effect.id } }
                        .map { it.name }
                    if (usedIn.isNotEmpty()) {
                        dispatch(IngredientFormMessage.DeleteBlockedBy(usedIn))
                    } else {
                        ingredientRepository.deleteIngredient(effect.id)
                        dispatch(IngredientFormMessage.DeleteSuccess)
                    }
                } catch (e: Exception) {
                    dispatch(IngredientFormMessage.ErrorOccurred(e.message ?: "Error al eliminar"))
                }
            }
        }
    }
}
