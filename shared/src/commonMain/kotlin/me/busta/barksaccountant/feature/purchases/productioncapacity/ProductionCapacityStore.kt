package me.busta.barksaccountant.feature.purchases.productioncapacity

import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class ProductionCapacityStore(
    private val productRepository: ProductRepository
) : Store<ProductionCapacityState, ProductionCapacityMessage, ProductionCapacityEffect>(ProductionCapacityState()) {

    override fun reduce(
        state: ProductionCapacityState,
        message: ProductionCapacityMessage
    ): Next<ProductionCapacityState, ProductionCapacityEffect> {
        return when (message) {
            is ProductionCapacityMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                ProductionCapacityEffect.LoadProducts
            )
            is ProductionCapacityMessage.ProductsLoaded -> Next.just(
                state.copy(availableProducts = message.products, isLoading = false)
            )
            is ProductionCapacityMessage.ProductPickerOpened -> Next.just(state.copy(showProductPicker = true))
            is ProductionCapacityMessage.DismissProductPicker -> Next.just(state.copy(showProductPicker = false))
            is ProductionCapacityMessage.ProductPicked -> Next.just(
                state.copy(
                    showProductPicker = false,
                    selectedProduct = message.product,
                    selectedIngredient = null,
                    availableQuantityText = ""
                )
            )
            is ProductionCapacityMessage.IngredientPickerOpened -> Next.just(state.copy(showIngredientPicker = true))
            is ProductionCapacityMessage.DismissIngredientPicker -> Next.just(state.copy(showIngredientPicker = false))
            is ProductionCapacityMessage.IngredientPicked -> Next.just(
                state.copy(
                    showIngredientPicker = false,
                    selectedIngredient = message.ingredient,
                    availableQuantityText = ""
                )
            )
            is ProductionCapacityMessage.AvailableQuantityChanged -> Next.just(
                state.copy(availableQuantityText = message.text)
            )
            is ProductionCapacityMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: ProductionCapacityEffect) {
        when (effect) {
            is ProductionCapacityEffect.LoadProducts -> {
                try {
                    val products = productRepository.getProducts()
                    dispatch(ProductionCapacityMessage.ProductsLoaded(products))
                } catch (e: Exception) {
                    dispatch(ProductionCapacityMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
