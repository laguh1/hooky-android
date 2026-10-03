package com.hooky.app.ui.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.R
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.premium.PremiumManager
import com.hooky.app.data.repository.PieceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val materialCost: Float?,   // per unit
    val price: Float?,          // per unit
    val soldPrice: Float?,      // per unit
    val workHours: Float?,
    val quantityTotal: Int,
    val salePlatform: String?,
    val totalCost: Float?,
    val totalRevenue: Float?,
    val profit: Float?,
)

data class CustomerReportRow(
    val name: String,
    val pieceCount: Int,
    val revenue: Float,
    val cost: Float,
    val profit: Float,
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val rows: List<PieceReportRow> = emptyList(),
    val customerRows: List<CustomerReportRow> = emptyList(),
    val totalRevenue: Float = 0f,
    val totalCost: Float = 0f,
    val totalProfit: Float = 0f,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
                    val revenue = rows.sumOf { (it.totalRevenue ?: 0f).toDouble() }.toFloat()
                    val cost = rows.sumOf { (it.totalCost ?: 0f).toDouble() }.toFloat()
                    val profit = revenue - cost
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rows = rows,
                            customerRows = buildCustomerRows(rows),
                            totalRevenue = revenue,
                            totalCost = cost,
                            totalProfit = profit,
                        )
                    }
                }
        }
    }

    private fun buildCustomerRows(rows: List<PieceReportRow>): List<CustomerReportRow> {
        val unassignedLabel = context.getString(R.string.reports_unassigned)
        return rows
            .groupBy { row -> row.salePlatform?.trim()?.ifBlank { null }?.lowercase() }
            .map { (key, groupRows) ->
                val displayName = if (key == null) unassignedLabel else groupRows.first().salePlatform!!.trim()
                CustomerReportRow(
                    name = displayName,
                    pieceCount = groupRows.size,
                    revenue = groupRows.sumOf { (it.totalRevenue ?: 0f).toDouble() }.toFloat(),
                    cost = groupRows.sumOf { (it.totalCost ?: 0f).toDouble() }.toFloat(),
                    profit = groupRows.sumOf { (it.profit ?: 0f).toDouble() }.toFloat(),
                )
            }
            .sortedByDescending { it.revenue }
    }

    fun buildCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Name,Status,Quantity,Material Cost,Price,Sold Price,Work Hours,Total Cost,Total Revenue,Profit")
        _uiState.value.rows.forEach { row ->
            sb.appendLine(
                "${row.name.escapeCsv()},${row.status},${row.quantityTotal}," +
                    "${row.materialCost ?: ""}," +
                    "${row.price ?: ""}," +
                    "${row.soldPrice ?: ""}," +
                    "${row.workHours ?: ""}," +
                    "${row.totalCost ?: ""}," +
                    "${row.totalRevenue ?: ""}," +
                    "${row.profit ?: ""}"
            )
        }
        return sb.toString()
    }

    fun buildCustomerCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Customer / Shop / Webpage,Pieces,Revenue,Cost,Profit")
        _uiState.value.customerRows.forEach { row ->
            sb.appendLine(
                "${row.name.escapeCsv()},${row.pieceCount},${row.revenue},${row.cost},${row.profit}"
            )
        }
        return sb.toString()
    }

    private fun PieceEntity.toReportRow(): PieceReportRow {
        val units = quantityTotal.coerceAtLeast(1)
        val unitRevenue = soldPrice ?: price
        val totalRevenue = unitRevenue?.let { it * units }
        val totalCost = materialCost?.let { it * units }
        val profit = if (totalRevenue != null && totalCost != null) totalRevenue - totalCost else null
        return PieceReportRow(
            name = name,
            status = workStatus,
            materialCost = materialCost,
            price = price,
            soldPrice = soldPrice,
            workHours = workHours,
            quantityTotal = units,
            salePlatform = salePlatform,
            totalCost = totalCost,
            totalRevenue = totalRevenue,
            profit = profit,
        )
    }

    private fun String.escapeCsv(): String =
        if (contains(',') || contains('"') || contains('\n')) "\"${replace("\"", "\"\"")}\"" else this
}
