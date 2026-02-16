package me.busta.barksaccountant.feature.settings.products.form

import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.model.Product
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductFormStore(
    private val productRepository: ProductRepository
) : Store<ProductFormState, ProductFormMessage, ProductFormEffect>(ProductFormState()) {

    override fun reduce(state: ProductFormState, message: ProductFormMessage): Next<ProductFormState, ProductFormEffect> {
        return when (message) {
            is ProductFormMessage.Started -> Next.withEffects(
                state.copy(
                    productId = message.productId,
                    isEditing = message.productId != null
                ),
                ProductFormEffect.LoadProduct(message.productId)
            )
            is ProductFormMessage.ProductLoaded -> {
                val product = message.product
                if (product != null) {
                    Next.just(state.copy(name = product.name, price = product.unitPrice.toString()))
                } else {
                    Next.just(state)
                }
            }
            is ProductFormMessage.NameChanged -> Next.just(state.copy(name = message.text))
            is ProductFormMessage.PriceChanged -> Next.just(state.copy(price = message.text))
            is ProductFormMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val product = Product(
                    id = state.productId ?: Uuid.random().toString(),
                    name = state.name,
                    unitPrice = state.price.toDouble()
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
}
