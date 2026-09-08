package com.hooky.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.backup.BackupService
import com.hooky.app.data.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupState {
    object Idle : BackupState()
    object Exporting : BackupState()
    data class ExportReady(val uri: Uri) : BackupState()
    object Importing : BackupState()
    data class ImportDone(val count: Int) : BackupState()
    data class Error(val message: String) : BackupState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
    private val backupService: BackupService
) : ViewModel() {
    val isPremium: StateFlow<Boolean> = premiumManager.isPremiumFlow
    val installationId: StateFlow<String> = premiumManager.installationIdFlow

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState

    fun setDebugPremium(enabled: Boolean) = premiumManager.forceSetPremium(enabled)

    fun export() {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting
            try {
                val uri = backupService.export()
                _backupState.value = BackupState.ExportReady(uri)
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Export failed: ${e.message}")
            }
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Importing
            val result = backupService.import(uri)
            _backupState.value = if (result.isSuccess) {
                BackupState.ImportDone(result.getOrDefault(0))
            } else {
                BackupState.Error("Import failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun clearBackupState() {
        _backupState.value = BackupState.Idle
    }
}
