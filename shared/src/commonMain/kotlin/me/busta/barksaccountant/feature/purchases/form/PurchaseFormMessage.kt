package me.busta.barksaccountant.feature.purchases.form

import me.busta.barksaccountant.model.Purchase

sealed interface PurchaseFormMessage {
    data class Started(val purchaseId: String?) : PurchaseFormMessage
    data class PurchaseLoaded(val purchase: Purchase?) : PurchaseFormMessage
    data class TitleChanged(val text: String) : PurchaseFormMessage
    data class DescriptionChanged(val text: String) : PurchaseFormMessage
    data class ValueChanged(val text: String) : PurchaseFormMessage
    data class DateChanged(val date: String) : PurchaseFormMessage
    data object SaveTapped : PurchaseFormMessage
    data object SaveSuccess : PurchaseFormMessage
    data object DeleteTapped : PurchaseFormMessage
    data object ConfirmDelete : PurchaseFormMessage
    data object DismissDelete : PurchaseFormMessage
    data object DeleteSuccess : PurchaseFormMessage
    data class ErrorOccurred(val error: String) : PurchaseFormMessage
}
