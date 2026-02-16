package me.busta.barksaccountant.data.repository

class InMemoryUserRepository : UserRepository {
    private val validUsers = mutableListOf("admin", "user1", "busta")

    override suspend fun validateUser(userId: String): Boolean {
        return validUsers.any { it.equals(userId, ignoreCase = true) }
    }
}
