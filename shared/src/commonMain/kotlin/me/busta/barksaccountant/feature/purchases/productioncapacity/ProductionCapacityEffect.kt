package me.busta.barksaccountant.feature.purchases.productioncapacity

sealed interface ProductionCapacityEffect {
    data object LoadProducts : ProductionCapacityEffect
}
