package me.busta.barksaccountant.feature.settings.businessinfo

import me.busta.barksaccountant.model.BusinessInfo

sealed interface BusinessInfoMessage {
    data object Started : BusinessInfoMessage
    data class InfoLoaded(val info: BusinessInfo?) : BusinessInfoMessage
    data class BusinessNameChanged(val text: String) : BusinessInfoMessage
    data class NifChanged(val text: String) : BusinessInfoMessage
    data class AddressChanged(val text: String) : BusinessInfoMessage
    data class PhoneChanged(val text: String) : BusinessInfoMessage
    data class EmailChanged(val text: String) : BusinessInfoMessage
    data class BankNameChanged(val text: String) : BusinessInfoMessage
    data class IbanChanged(val text: String) : BusinessInfoMessage
    data class BankHolderChanged(val text: String) : BusinessInfoMessage
    data object SaveTapped : BusinessInfoMessage
    data object SaveSuccess : BusinessInfoMessage
    data class ErrorOccurred(val error: String) : BusinessInfoMessage
}
