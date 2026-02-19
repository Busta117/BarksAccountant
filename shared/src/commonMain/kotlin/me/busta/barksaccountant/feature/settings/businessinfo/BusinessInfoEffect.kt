package me.busta.barksaccountant.feature.settings.businessinfo

import me.busta.barksaccountant.model.BusinessInfo

sealed interface BusinessInfoEffect {
    data object LoadInfo : BusinessInfoEffect
    data class SaveInfo(val info: BusinessInfo) : BusinessInfoEffect
}
