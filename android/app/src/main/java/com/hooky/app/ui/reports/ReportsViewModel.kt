package com.hooky.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.premium.PremiumManager
import com.hooky.app.data.repository.PieceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PieceReportRow(
    val name: String,
    val status: String,
    val materialCost: Float?,
    val price: Float?,
    val soldPrice: Float?,
    val workHours: Float?,
    val profit: Float?,
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val rows: List<PieceReportRow> = emptyList(),
    val totalRevenue: Float = 0f,
    val totalCost: Float = 0f,
    val totalProfit: Float = 0f,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    private val premiumManager: PremiumManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            premiumManager.isPremiumFlow.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
        viewModelScope.launch {
            pieceRepository.getAllPieces()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { pieces ->
                    val rows = pieces
                        .filter { !it.archived }
                        .sortedByDescending { it.updatedAt }
                        .map { it.toReportRow() }
                    val revenue = rows.sumOf { (it.soldPrice ?: it.price ?: 0f).toDouble() }.toFloat()
                    val cost = rows.sumOf { (it.materialCost ?: 0f).toDouble() }.toFloat()
                    val profit = revenue - cost
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rows = rows,
                            totalRevenue = revenue,
                            totalCost = cost,
                            totalProfit = profit,
                        )
                    }
                }
        }
    }

    fun buildCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Name,Status,Material Cost,Price,Sold Price,Work Hours,Profit")
        _uiState.value.rows.forEach { row ->
            sb.appendLine(
                "${row.name.escapeCsv()},${row.status}," +
                    "${row.materialCost ?: ""}," +
                    "${row.price ?: ""}," +
                    "${row.soldPrice ?: ""}," +
                    "${row.workHours ?: ""}," +
                    "${row.profit ?: ""}"
            )
        }
        return sb.toString()
    }

    private fun PieceEntity.toReportRow(): PieceReportRow {
        val revenue = soldPrice ?: price
        val profit = if (revenue != null && materialCost != null) revenue - materialCost else null
        return PieceReportRow(
            name = name,
            status = workStatus,
            materialCost = materialCost,
            price = price,
            soldPrice = soldPrice,
            workHours = workHours,
            profit = profit,
        )
    }

    private fun String.escapeCsv(): String =
        if (contains(',') || contains('"') || contains('\n')) "\"${replace("\"", "\"\"")}\"" else this
}
