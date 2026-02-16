package me.busta.barksaccountant.feature.sales.detail

import me.busta.barksaccountant.model.Sale

sealed interface SaleDetailMessage {
    data class Started(val saleId: String) : SaleDetailMessage
    data class SaleLoaded(val sale: Sale) : SaleDetailMessage
    data object MarkAsPaidTapped : SaleDetailMessage
    data object ConfirmPaid : SaleDetailMessage
    data object MarkAsDeliveredTapped : SaleDetailMessage
    data object ConfirmDelivered : SaleDetailMessage
    data class SaleUpdated(val sale: Sale) : SaleDetailMessage
    data object DismissConfirm : SaleDetailMessage
    data class ErrorOccurred(val error: String) : SaleDetailMessage
}
