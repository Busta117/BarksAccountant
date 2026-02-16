package me.busta.barksaccountant.feature.settings.products.list

import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class ProductsListStore(
    private val productRepository: ProductRepository
) : Store<ProductsListState, ProductsListMessage, ProductsListEffect>(ProductsListState()) {

    override fun reduce(state: ProductsListState, message: ProductsListMessage): Next<ProductsListState, ProductsListEffect> {
        return when (message) {
            is ProductsListMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                ProductsListEffect.LoadProducts
            )
            is ProductsListMessage.ProductsLoaded -> Next.just(
                state.copy(products = message.products, isLoading = false, error = null)
            )
            is ProductsListMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: ProductsListEffect) {
        when (effect) {
            is ProductsListEffect.LoadProducts -> {
                try {
                    val products = productRepository.getProducts()
                    dispatch(ProductsListMessage.ProductsLoaded(products))
                } catch (e: Exception) {
                    dispatch(ProductsListMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
