package com.hooky.app.ui.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.R
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.premium.PremiumManager
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.data.repository.YarnRepository
import com.hooky.app.domain.model.ballsUsedByYarn
import com.hooky.app.ui.yarns.UNKNOWN_YARN_COLOR
import com.hooky.app.util.XlsxCell
import com.hooky.app.util.XlsxSheet
import com.hooky.app.util.XlsxWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    val soldDate: String?,
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

data class ReportExportLabels(
    val piecesSheet: String,
    val customersSheet: String,
    val yarnsSheet: String,
    val piecesHeader: List<String>,
    val customersHeader: List<String>,
    val yarnsHeader: List<String>,
    val total: String,
    val statusLabels: Map<String, String>,
    val materialLabels: Map<String, String>,
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val rows: List<PieceReportRow> = emptyList(),
    val customerRows: List<CustomerReportRow> = emptyList(),
    val yarns: List<YarnEntity> = emptyList(),
    // Balls used per yarn ID across non-archived pieces
    val ballsUsedByYarn: Map<String, Float> = emptyMap(),
    val totalRevenue: Float = 0f,
    val totalCost: Float = 0f,
    val totalProfit: Float = 0f,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pieceRepository: PieceRepository,
    private val yarnRepository: YarnRepository,
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
                    // Only pieces for sale or sold: price and sale fields are hidden (but kept)
                    // for gifts and own pieces, so they must not count as revenue.
                    val rows = pieces
                        .filter { !it.archived && it.destination in SALE_DESTINATIONS }
                        .sortedByDescending { it.updatedAt }
                        .map { it.toReportRow() }
                    val revenue = rows.sumOf { (it.totalRevenue ?: 0f).toDouble() }.toFloat()
                    val cost = rows.sumOf { (it.totalCost ?: 0f).toDouble() }.toFloat()
                    // Same rule as each row and the "by customer" tab: a piece adds to profit only
                    // when both its price and its cost are known (before, the total counted the
                    // full price of pieces with no cost as profit, so totals didn't match the rows).
                    val profit = rows.sumOf { (it.profit ?: 0f).toDouble() }.toFloat()
                    val ballsUsed = ballsUsedByYarn(pieces.filter { !it.archived }.map { it.yarnsUsed })
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rows = rows,
                            ballsUsedByYarn = ballsUsed,
                            customerRows = buildCustomerRows(rows),
                            totalRevenue = revenue,
                            totalCost = cost,
                            totalProfit = profit,
                        )
                    }
                }
        }
    }

    init {
        viewModelScope.launch {
            yarnRepository.getAllYarns()
                .catch { }
                .collect { yarns -> _uiState.update { it.copy(yarns = yarns.filter { y -> !y.archived }) } }
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

    // Labels come from the screen so the export follows the app language.
    fun buildWorkbook(labels: ReportExportLabels): List<XlsxSheet> {
        val state = _uiState.value
        fun num(v: Float?) = v?.let { XlsxCell.Number(it.toDouble()) } ?: XlsxCell.Empty
        fun int(v: Int?) = v?.let { XlsxCell.Number(it.toDouble(), decimals = false) } ?: XlsxCell.Empty
        fun text(v: String?) = v?.takeIf { it.isNotBlank() }?.let { XlsxCell.Text(it) } ?: XlsxCell.Empty
        // Real date cells, so each spreadsheet shows them in its own format. Accepts dd/MM/yyyy
        // too: those used to go out as text that US-English Google Sheets read as MM/dd.
        fun date(v: String?) = v?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
                ?: runCatching { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()
        }?.let { XlsxCell.Date(it) } ?: text(v)
        // SUM over the data rows of one column; data starts on row 2, after the header
        fun sum(col: Int, count: Int, decimals: Boolean = true): XlsxCell =
            if (count == 0) XlsxCell.Empty
            else XlsxWriter.column(col).let { c -> XlsxCell.Formula("SUM(${c}2:$c${count + 1})", decimals) }

        val pieces = XlsxSheet(
            name = labels.piecesSheet,
            header = labels.piecesHeader,
            rows = state.rows.map { r ->
                listOf(
                    XlsxCell.Text(r.name), text(labels.statusLabels[r.status] ?: r.status), int(r.quantityTotal),
                    num(r.materialCost), num(r.price), num(r.soldPrice), num(r.workHours),
                    num(r.totalCost), num(r.totalRevenue), num(r.profit), text(r.salePlatform), date(r.soldDate)
                )
            },
            totals = listOf(XlsxCell.Text(labels.total), XlsxCell.Empty, sum(2, state.rows.size, decimals = false), XlsxCell.Empty,
                XlsxCell.Empty, XlsxCell.Empty, sum(6, state.rows.size), sum(7, state.rows.size),
                sum(8, state.rows.size), sum(9, state.rows.size)),
            columnWidths = listOf(28, 14, 10, 14, 14, 14, 12, 14, 14, 14, 22, 14),
        )
        val customers = XlsxSheet(
            name = labels.customersSheet,
            header = labels.customersHeader,
            rows = state.customerRows.map { r ->
                listOf(XlsxCell.Text(r.name), int(r.pieceCount), num(r.revenue), num(r.cost), num(r.profit))
            },
            totals = listOf(XlsxCell.Text(labels.total)) + (1..4).map { sum(it, state.customerRows.size, decimals = it != 1) },
            columnWidths = listOf(28, 10, 14, 14, 14),
        )
        val yarns = XlsxSheet(
            name = labels.yarnsSheet,
            header = labels.yarnsHeader,
            rows = state.yarns.mapIndexed { i, y ->
                val row = i + 2
                val used = state.ballsUsedByYarn[y.yarnId] ?: 0f
                val left = y.quantityOwned?.let { (it - used).coerceAtLeast(0f) }
                // Total spent = price per ball × balls bought, as a formula so edits in Excel update it
                val spent = if (y.pricePaid != null && y.quantityOwned != null) XlsxCell.Formula("G$row*H$row") else XlsxCell.Empty
                listOf(
                    XlsxCell.Text(y.name), text(y.brand), text(y.color.takeUnless { it == UNKNOWN_YARN_COLOR }),
                    text(labels.materialLabels[y.material] ?: y.material), text(y.purchaseLocation), date(y.purchaseDate),
                    num(y.pricePaid), int(y.quantityOwned), if (used > 0f) num(used) else XlsxCell.Empty, num(left),
                    spent, text(y.notes)
                )
            },
            totals = listOf(XlsxCell.Text(labels.total)) + List(6) { XlsxCell.Empty } +
                listOf(sum(7, state.yarns.size, decimals = false), sum(8, state.yarns.size), sum(9, state.yarns.size), sum(10, state.yarns.size)),
            columnWidths = listOf(24, 16, 16, 14, 20, 14, 14, 12, 12, 12, 14, 30),
        )
        return listOf(pieces, customers, yarns)
    }

    /** Writes the .xlsx into [dir] and returns it. Runs off the main thread. */
    suspend fun exportXlsx(dir: File, labels: ReportExportLabels): File = withContext(Dispatchers.IO) {
        dir.mkdirs()
        // Only the latest export is kept, so old ones don't pile up in app storage
        dir.listFiles { f -> f.name.endsWith(".xlsx") }?.forEach { it.delete() }
        val file = File(dir, "hooky-app-${LocalDate.now()}.xlsx")
        file.outputStream().use { XlsxWriter.write(buildWorkbook(labels), it) }
        file
    }

    private fun PieceEntity.toReportRow(): PieceReportRow {
        val units = quantityTotal.coerceAtLeast(1)
        // A sold price only applies once the piece is marked Sold (it is hidden otherwise).
        val effectiveSoldPrice = soldPrice.takeIf { destination == "SOLD" }
        val unitRevenue = effectiveSoldPrice ?: price
        val totalRevenue = unitRevenue?.let { it * units }
        val totalCost = materialCost?.let { it * units }
        val profit = if (totalRevenue != null && totalCost != null) totalRevenue - totalCost else null
        return PieceReportRow(
            name = name,
            status = workStatus,
            materialCost = materialCost,
            price = price,
            soldPrice = effectiveSoldPrice,
            workHours = workHours,
            quantityTotal = units,
            salePlatform = salePlatform,
            soldDate = soldDate,
            totalCost = totalCost,
            totalRevenue = totalRevenue,
            profit = profit,
        )
    }
}

private val SALE_DESTINATIONS = setOf("FOR_SALE", "SOLD")
