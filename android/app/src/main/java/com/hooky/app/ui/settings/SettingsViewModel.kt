package com.hooky.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.backup.BackupPreview
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
    object ExportSaved : BackupState()
    /** A backup file was picked: show what it contains and ask before replacing everything. */
    data class ConfirmImport(val uri: Uri, val preview: BackupPreview) : BackupState()
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

    private val _canUndoImport = MutableStateFlow(backupService.hasSafetyCopy())
    val canUndoImport: StateFlow<Boolean> = _canUndoImport

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

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting
            _backupState.value = try {
                backupService.exportTo(uri)
                BackupState.ExportSaved
            } catch (e: Exception) {
                BackupState.Error("Export failed: ${e.message}")
            }
        }
    }

    /** Step 1 of an import: read the file and ask the user to confirm. */
    fun prepareImport(uri: Uri) {
        viewModelScope.launch {
            val preview = backupService.preview(uri)
            _backupState.value = preview.fold(
                onSuccess = { BackupState.ConfirmImport(uri, it) },
                onFailure = { BackupState.Error("Import failed: ${it.message}") }
            )
        }
    }

    /** Step 2: the user confirmed. The current data is kept as a safety copy first. */
    fun import(uri: Uri) = runImport { backupService.import(uri) }

    fun undoLastImport() = runImport { backupService.restoreSafetyCopy() }

    private fun runImport(block: suspend () -> Result<Int>) {
        viewModelScope.launch {
            _backupState.value = BackupState.Importing
            val result = block()
            _canUndoImport.value = backupService.hasSafetyCopy()
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
