package com.hooky.app.ui.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.premium.PremiumManager
import com.hooky.app.data.premium.ProBillingManager
import com.hooky.app.data.premium.ProOffer
import com.hooky.app.data.premium.ProPurchaseEvent
import com.hooky.app.data.premium.ProStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
    private val billing: ProBillingManager
) : ViewModel() {
    val status: StateFlow<ProStatus> = premiumManager.proStatusFlow
    val offer: StateFlow<ProOffer?> = billing.offerFlow
    val events: SharedFlow<ProPurchaseEvent> = billing.events

    private val _isLoadingOffer = MutableStateFlow(true)
    val isLoadingOffer: StateFlow<Boolean> = _isLoadingOffer.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoadingOffer.value = true
            premiumManager.refreshPurchases()
            _isLoadingOffer.value = false
        }
    }

    fun buy(activity: Activity) {
        billing.launchPurchase(activity)
    }
}
