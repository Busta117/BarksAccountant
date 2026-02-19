package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.model.BusinessInfo

interface BusinessInfoRepository {
    suspend fun getBusinessInfo(): BusinessInfo?
    suspend fun saveBusinessInfo(info: BusinessInfo)
}
