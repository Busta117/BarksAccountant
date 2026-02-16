package me.busta.barksaccountant.di

import me.busta.barksaccountant.data.LocalStorage
import me.busta.barksaccountant.data.repository.*

class ServiceLocator(val localStorage: LocalStorage) {
    val userRepository: UserRepository by lazy { InMemoryUserRepository() }
    val saleRepository: SaleRepository by lazy { InMemorySaleRepository() }
    val productRepository: ProductRepository by lazy { InMemoryProductRepository() }
    val clientRepository: ClientRepository by lazy { InMemoryClientRepository() }
}
