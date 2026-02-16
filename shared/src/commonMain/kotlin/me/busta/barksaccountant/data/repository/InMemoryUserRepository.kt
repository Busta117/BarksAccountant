package me.busta.barksaccountant.data.repository

class InMemoryAppIdRepository : AppIdRepository {
    private val validAppIds = listOf("barksnbites")

    override suspend fun validateAppId(appId: String): Boolean {
        return validAppIds.any { it.equals(appId, ignoreCase = true) }
    }
}
