package me.busta.barksaccountant.model

data class BusinessInfo(
    val businessName: String,
    val nif: String?,
    val address: String?,
    val phone: String?,
    val email: String?,
    val bankName: String?,
    val iban: String?,
    val bankHolder: String?
)
