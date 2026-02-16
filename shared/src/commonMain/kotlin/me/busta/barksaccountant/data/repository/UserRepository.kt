package me.busta.barksaccountant.data.repository

interface AppIdRepository {
    suspend fun validateAppId(appId: String): Boolean
}
