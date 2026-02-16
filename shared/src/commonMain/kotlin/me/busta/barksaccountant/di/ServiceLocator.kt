package me.busta.barksaccountant.di

import me.busta.barksaccountant.data.FirestoreService
import me.busta.barksaccountant.data.LocalStorage
import me.busta.barksaccountant.data.repository.*

class ServiceLocator(
    val localStorage: LocalStorage,
    val firestoreService: FirestoreService
) {
    var appId: String = ""

    val appIdRepository: AppIdRepository get() = FirestoreAppIdRepository(firestoreService)
    val saleRepository: SaleRepository get() = FirestoreSaleRepository(firestoreService, appId)
    val productRepository: ProductRepository get() = FirestoreProductRepository(firestoreService, appId)
    val clientRepository: ClientRepository get() = FirestoreClientRepository(firestoreService, appId)
    val purchaseRepository: PurchaseRepository get() = FirestorePurchaseRepository(firestoreService, appId)
}
