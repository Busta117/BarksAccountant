package me.busta.barksaccountant.feature.settings.businessinfo

import me.busta.barksaccountant.data.repository.BusinessInfoRepository
import me.busta.barksaccountant.model.BusinessInfo
import me.busta.barksaccountant.store.Next
import me.busta.barksaccountant.store.Store

class BusinessInfoStore(
    private val businessInfoRepository: BusinessInfoRepository
) : Store<BusinessInfoState, BusinessInfoMessage, BusinessInfoEffect>(BusinessInfoState()) {

    override fun reduce(state: BusinessInfoState, message: BusinessInfoMessage): Next<BusinessInfoState, BusinessInfoEffect> {
        return when (message) {
            is BusinessInfoMessage.Started -> Next.withEffects(state, BusinessInfoEffect.LoadInfo)
            is BusinessInfoMessage.InfoLoaded -> {
                val info = message.info
                if (info != null) {
                    Next.just(state.copy(
                        businessName = info.businessName,
                        nif = info.nif ?: "",
                        address = info.address ?: "",
                        phone = info.phone ?: "",
                        email = info.email ?: "",
                        bankName = info.bankName ?: "",
                        iban = info.iban ?: "",
                        bankHolder = info.bankHolder ?: ""
                    ))
                } else {
                    Next.just(state)
                }
            }
            is BusinessInfoMessage.BusinessNameChanged -> Next.just(state.copy(businessName = message.text))
            is BusinessInfoMessage.NifChanged -> Next.just(state.copy(nif = message.text))
            is BusinessInfoMessage.AddressChanged -> Next.just(state.copy(address = message.text))
            is BusinessInfoMessage.PhoneChanged -> Next.just(state.copy(phone = message.text))
            is BusinessInfoMessage.EmailChanged -> Next.just(state.copy(email = message.text))
            is BusinessInfoMessage.BankNameChanged -> Next.just(state.copy(bankName = message.text))
            is BusinessInfoMessage.IbanChanged -> Next.just(state.copy(iban = message.text))
            is BusinessInfoMessage.BankHolderChanged -> Next.just(state.copy(bankHolder = message.text))
            is BusinessInfoMessage.SaveTapped -> {
                if (!state.canSave) return Next.just(state)
                val info = BusinessInfo(
                    businessName = state.businessName,
                    nif = state.nif.ifBlank { null },
                    address = state.address.ifBlank { null },
                    phone = state.phone.ifBlank { null },
                    email = state.email.ifBlank { null },
                    bankName = state.bankName.ifBlank { null },
                    iban = state.iban.ifBlank { null },
                    bankHolder = state.bankHolder.ifBlank { null }
                )
                Next.withEffects(state.copy(isSaving = true, error = null), BusinessInfoEffect.SaveInfo(info))
            }
            is BusinessInfoMessage.SaveSuccess -> Next.just(state.copy(isSaving = false, savedSuccessfully = true))
            is BusinessInfoMessage.ErrorOccurred -> Next.just(state.copy(isSaving = false, error = message.error))
        }
    }

    override suspend fun handleEffect(effect: BusinessInfoEffect) {
        when (effect) {
            is BusinessInfoEffect.LoadInfo -> {
                try {
                    val info = businessInfoRepository.getBusinessInfo()
                    dispatch(BusinessInfoMessage.InfoLoaded(info))
                } catch (e: Exception) {
                    dispatch(BusinessInfoMessage.ErrorOccurred(e.message ?: "Error desconocido"))
                }
            }
            is BusinessInfoEffect.SaveInfo -> {
                try {
                    businessInfoRepository.saveBusinessInfo(effect.info)
                    dispatch(BusinessInfoMessage.SaveSuccess)
                } catch (e: Exception) {
                    dispatch(BusinessInfoMessage.ErrorOccurred(e.message ?: "Error al guardar"))
                }
            }
        }
    }
}
