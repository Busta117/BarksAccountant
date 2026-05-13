package me.busta.barksaccountant.feature.purchases.productionplan

sealed interface ProductionPlanEffect {
    data object LoadProducts : ProductionPlanEffect
}
