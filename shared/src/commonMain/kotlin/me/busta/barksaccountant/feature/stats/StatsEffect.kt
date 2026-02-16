package me.busta.barksaccountant.feature.stats

sealed interface StatsEffect {
    data object LoadData : StatsEffect
}
