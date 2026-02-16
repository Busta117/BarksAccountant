package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.data.FirestoreService

class FirestoreAppIdRepository(
    private val firestoreService: FirestoreService
) : AppIdRepository {

    override suspend fun validateAppId(appId: String): Boolean {
        val doc = firestoreService.getDocument("app_ids", appId)
        return doc != null
    }
}
