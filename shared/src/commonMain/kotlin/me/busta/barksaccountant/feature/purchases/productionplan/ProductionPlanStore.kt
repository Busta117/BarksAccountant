package me.busta.barksaccountant.feature.purchases.productionplan

import me.busta.barksaccountant.data.repository.ProductRepository
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class ProductionPlanStore(
    private val productRepository: ProductRepository
) : Store<ProductionPlanState, ProductionPlanMessage, ProductionPlanEffect>(ProductionPlanState()) {

    override fun reduce(
        state: ProductionPlanState,
        message: ProductionPlanMessage
    ): Next<ProductionPlanState, ProductionPlanEffect> {
        return when (message) {
            is ProductionPlanMessage.Started -> Next.withEffects(
                state.copy(isLoading = true, error = null),
                ProductionPlanEffect.LoadProducts
            )
            is ProductionPlanMessage.ProductsLoaded -> Next.just(
                state.copy(availableProducts = message.products, isLoading = false)
            )
            is ProductionPlanMessage.AddProductTapped -> Next.just(state.copy(showProductPicker = true))
            is ProductionPlanMessage.DismissPicker -> Next.just(state.copy(showProductPicker = false))
            is ProductionPlanMessage.ProductPicked -> {
                val already = state.rows.any { it.productId == message.product.id }
                if (already) return Next.just(state.copy(showProductPicker = false))
                Next.just(
                    state.copy(
                        showProductPicker = false,
                        rows = state.rows + PlanRow(
                            productId = message.product.id,
                            productName = message.product.name,
                            quantityText = "1"
                        )
                    )
                )
            }
            is ProductionPlanMessage.QuantityChanged -> {
                Next.just(
                    state.copy(
                        rows = state.rows.map { row ->
                            if (row.productId == message.productId) row.copy(quantityText = message.text)
                            else row
                        }
                    )
                )
            }
            is ProductionPlanMessage.RowRemoved -> Next.just(
                state.copy(rows = state.rows.filterNot { it.productId == message.productId })
            )
            is ProductionPlanMessage.ErrorOccurred -> Next.just(
                state.copy(isLoading = false, error = message.error)
            )
        }
    }

    override suspend fun handleEffect(effect: ProductionPlanEffect) {
        when (effect) {
            is ProductionPlanEffect.LoadProducts -> {
                try {
                    val products = productRepository.getProducts()
                    dispatch(ProductionPlanMessage.ProductsLoaded(products))
                } catch (e: Exception) {
                    dispatch(ProductionPlanMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
        }
    }
}
