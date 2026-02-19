package me.busta.barksaccountant.feature.settings.businessinfo

data class BusinessInfoState(
    val businessName: String = "",
    val nif: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val bankName: String = "",
    val iban: String = "",
    val bankHolder: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean get() = businessName.isNotBlank()
}
