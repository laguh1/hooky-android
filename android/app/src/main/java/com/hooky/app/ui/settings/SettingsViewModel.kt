package com.hooky.app.ui.settings

import androidx.lifecycle.ViewModel
import com.hooky.app.data.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val premiumManager: PremiumManager
) : ViewModel() {
    val isPremium: StateFlow<Boolean> = premiumManager.isPremiumFlow
    val installationId: StateFlow<String> = premiumManager.installationIdFlow

    fun setDebugPremium(enabled: Boolean) = premiumManager.forceSetPremium(enabled)
}
