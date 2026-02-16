package me.busta.barksaccountant.data.repository

interface UserRepository {
    suspend fun validateUser(userId: String): Boolean
}
