package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.data.repository.IngredientRepository
import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.model.ProductIngredient
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductFormStore(
    private val productRepository: ProductRepository,
    private val ingredientRepository: IngredientRepository
) : Store<ProductFormState, ProductFormMessage, ProductFormEffect>(ProductFormState()) {

    override fun reduce(state: ProductFormState, message: ProductFormMessage): Next<ProductFormState, ProductFormEffect> {
        return when (message) {
            is ProductFormMessage.Started -> Next.withEffects(
                state.copy(
                    productId = message.productId,
                    isEditing = message.productId != null
                ),
                ProductFormEffect.LoadProduct(message.productId),
                ProductFormEffect.LoadIngredients
            )
            is ProductFormMessage.ProductLoaded -> {
                val product = message.product ?: return Next.just(state)
                Next.just(
                    state.copy(
                        name = product.name,
                        price = product.unitPrice.toString(),
                        ingredients = product.ingredients,
                        ingredientQuantityTexts = product.ingredients.map { formatQty(it.quantity) }
                    )
                )
            }
            is ProductFormMessage.IngredientsLoaded -> Next.just(state.copy(availableIngredients = message.ingredients))
            is ProductFormMessage.NameChanged -> Next.just(state.copy(name = message.text))
            is ProductFormMessage.PriceChanged -> Next.just(state.copy(price = message.text))
            is ProductFormMessage.AddIngredientTapped -> Next.just(state.copy(showIngredientPicker = true))
            is ProductFormMessage.DismissIngredientPicker -> Next.just(state.copy(showIngredientPicker = false))
            is ProductFormMessage.IngredientPicked -> {
                val already = state.ingredients.any { it.ingredientId == message.ingredient.id }
                if (already) return Next.just(state.copy(showIngredientPicker = false))
                val newIng = ProductIngredient(
                    ingredientId = message.ingredient.id,
                    ingredientName = message.ingredient.name,
                    unit = message.ingredient.unit,
                    quantity = 0.0
                )
                Next.just(
                    state.copy(
                        showIngredientPicker = false,
                        ingredients = state.ingredients + newIng,
                        ingredientQuantityTexts = state.ingredientQuantityTexts + ""
                    )
                )
            }
            is ProductFormMessage.IngredientQuantityChanged -> {
                val idx = message.index
                if (idx !in state.ingredients.indices) return Next.just(state)
                val newTexts = state.ingredientQuantityTexts.toMutableList().apply { this[idx] = message.text }
                val parsed = message.text.toDoubleOrNull() ?: 0.0
                val newIngs = state.ingredients.toMutableList().apply {
                    this[idx] = this[idx].copy(quantity = parsed)
                }
                Next.just(state.copy(ingredients = newIngs, ingredientQuantityTexts = newTexts))
            }
            is ProductFormMessage.IngredientRemoved -> {
                val idx = message.index
                if (idx !in state.ingredients.indices) return Next.just(state)
                Next.just(
                    state.copy(
                        ingredients = state.ingredients.toMutableList().apply { removeAt(idx) },
                        ingredientQuantityTexts = state.ingredientQuantityTexts.toMutableList().apply { removeAt(idx) }
                    )
                )
            }
            is ProductFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val product = Product(
                    id = state.productId ?: Uuid.random().toString(),
                    name = state.name,
                    unitPrice = state.price.toDouble(),
                    ingredients = state.ingredients
                )
                if (state.isEditing) {
                    Next.withEffects(state.copy(isSaving = true, error = null), ProductFormEffect.UpdateProduct(product))
                } else {
                    Next.withEffects(state.copy(isSaving = true, error = null), ProductFormEffect.SaveProduct(product))
                }
            }
            is ProductFormMessage.SaveSuccess -> Next.just(state.copy(isSaving = false, savedSuccessfully = true))
            is ProductFormMessage.DeleteTapped -> Next.just(state.copy(showDeleteConfirm = true))
            is ProductFormMessage.ConfirmDelete -> {
                val id = state.productId ?: return Next.just(state)
                Next.withEffects(state.copy(showDeleteConfirm = false, isSaving = true), ProductFormEffect.DeleteProduct(id))
            }
            is ProductFormMessage.DismissDelete -> Next.just(state.copy(showDeleteConfirm = false))
            is ProductFormMessage.DeleteSuccess -> Next.just(state.copy(isSaving = false, deletedSuccessfully = true))
            is ProductFormMessage.ErrorOccurred -> Next.just(state.copy(isSaving = false, error = message.error))
        }
    }

    override suspend fun handleEffect(effect: ProductFormEffect) {
        when (effect) {
            is ProductFormEffect.LoadProduct -> {
                try {
                    val product = effect.productId?.let { productRepository.getProduct(it) }
                    dispatch(ProductFormMessage.ProductLoaded(product))
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is ProductFormEffect.LoadIngredients -> {
                try {
                    val list = ingredientRepository.getIngredients()
                    dispatch(ProductFormMessage.IngredientsLoaded(list))
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al cargar ingredientes"))
                }
            }
            is ProductFormEffect.SaveProduct -> {
                try {
                    productRepository.saveProduct(effect.product)
                    dispatch(ProductFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
            is ProductFormEffect.UpdateProduct -> {
                try {
                    productRepository.updateProduct(effect.product)
                    dispatch(ProductFormMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al actualizar"))
                }
            }
            is ProductFormEffect.DeleteProduct -> {
                try {
                    productRepository.deleteProduct(effect.id)
                    dispatch(ProductFormMessage.DeleteSuccess)
                } catch (e: Exception) {
                    dispatch(ProductFormMessage.ErrorOccurred(e.message ?: "Error al eliminar"))
                }
            }
        }
    }

    private fun formatQty(q: Double): String {
        return if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()
    }
}
