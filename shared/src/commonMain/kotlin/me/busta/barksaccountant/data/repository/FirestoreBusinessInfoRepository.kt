package me.busta.barksaccountant.data.repository

import me.busta.barksaccountant.data.FirestoreService
import me.busta.barksaccountant.model.BusinessInfo

class FirestoreBusinessInfoRepository(
    private val firestoreService: FirestoreService,
    private val appId: String
) : BusinessInfoRepository {

    override suspend fun getBusinessInfo(): BusinessInfo? {
        val data = firestoreService.getDocument("app_ids", appId) ?: return null
        val businessName = data["businessName"] as? String ?: return null
        return BusinessInfo(
            businessName = businessName,
            nif = data["nif"] as? String,
            address = data["address"] as? String,
            phone = data["phone"] as? String,
            email = data["email"] as? String,
            bankName = data["bankName"] as? String,
            iban = data["iban"] as? String,
            bankHolder = data["bankHolder"] as? String
        )
    }

    override suspend fun saveBusinessInfo(info: BusinessInfo) {
        val map = mutableMapOf<String, Any>(
            "businessName" to info.businessName
        )
        info.nif?.let { map["nif"] = it }
        info.address?.let { map["address"] = it }
        info.phone?.let { map["phone"] = it }
        info.email?.let { map["email"] = it }
        info.bankName?.let { map["bankName"] = it }
        info.iban?.let { map["iban"] = it }
        info.bankHolder?.let { map["bankHolder"] = it }
        firestoreService.setDocument("app_ids", appId, map)
    }
}
