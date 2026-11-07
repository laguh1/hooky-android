package com.hooky.app.ui.reports

import androidx.compose.ui.text.style.TextOverflow
import com.hooky.app.ui.components.WordSafeText
import com.hooky.app.ui.util.labelResId
import com.hooky.app.domain.model.enums.WorkStatus
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.hooky.app.R
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.FileProvider
import com.hooky.app.domain.model.enums.Material
import kotlinx.coroutines.launch
import java.io.File
import com.hooky.app.ui.settings.formatCurrency
import com.hooky.app.ui.settings.getCurrencySymbol
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary

private enum class ReportTab { PIECES, CUSTOMERS }

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPro: () -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(ReportTab.PIECES) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // weight(1f) so a long translated title wraps instead of squeezing the export icon
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.reports_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // One workbook with all three tabs, whichever tab is showing
                val hasData = uiState.rows.isNotEmpty() || uiState.customerRows.isNotEmpty() || uiState.yarns.isNotEmpty()
                if (uiState.isPremium && hasData) {
                    val exportLabel = stringResource(R.string.reports_export_excel)
                    val subject = stringResource(R.string.reports_share_subject)
                    val labels = ReportExportLabels(
                        piecesSheet = stringResource(R.string.reports_sheet_pieces),
                        customersSheet = stringResource(R.string.reports_sheet_customers),
                        yarnsSheet = stringResource(R.string.reports_sheet_yarns),
                        piecesHeader = stringResource(R.string.reports_xlsx_header_pieces).split(','),
                        customersHeader = stringResource(R.string.reports_xlsx_header_customers).split(','),
                        yarnsHeader = stringResource(R.string.reports_xlsx_header_yarns).split(','),
                        total = stringResource(R.string.reports_total),
                        statusLabels = WorkStatus.values().associate { it.name to stringResource(it.labelResId) },
                        materialLabels = Material.values().associate { it.name to stringResource(it.labelResId) },
                    )
                    IconButton(onClick = {
                        scope.launch {
                            val file = viewModel.exportXlsx(File(context.filesDir, "share"), labels)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = XLSX_MIME
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, exportLabel))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = exportLabel,
                            tint = Slate
                        )
                    }
                }
            }

            if (uiState.isPremium && !uiState.isLoading) {
                ReportTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Slate)
                    }
                }
                !uiState.isPremium -> {
                    PremiumGate(onSeePro = onNavigateToPro)
                }
                selectedTab == ReportTab.CUSTOMERS -> {
                    CustomerReportsContent(uiState = uiState)
                }
                else -> {
                    ReportsContent(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun ReportTabRow(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedTab == ReportTab.PIECES,
            onClick = { onTabSelected(ReportTab.PIECES) },
            label = { Text(stringResource(R.string.reports_tab_pieces)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = androidx.compose.ui.graphics.Color.White
            )
        )
        FilterChip(
            selected = selectedTab == ReportTab.CUSTOMERS,
            onClick = { onTabSelected(ReportTab.CUSTOMERS) },
            label = { Text(stringResource(R.string.reports_tab_customers)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = androidx.compose.ui.graphics.Color.White
            )
        )
    }
}

@Composable
private fun PremiumGate(onSeePro: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.pro_gate_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.pro_gate_reports),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onSeePro,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text(stringResource(R.string.pro_gate_button))
            }
        }
    }
}

@Composable
private fun ReportsContent(uiState: ReportsUiState) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SummaryCard(
                totalRevenue = uiState.totalRevenue,
                totalCost = uiState.totalCost,
                totalProfit = uiState.totalProfit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            ReportTableHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        items(items = uiState.rows) { row ->
            ReportTableRow(
                row = row,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SummaryCard(
    totalRevenue: Float,
    totalCost: Float,
    totalProfit: Float,
    modifier: Modifier = Modifier,
) {
    val sym = getCurrencySymbol(LocalContext.current)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryCell(label = stringResource(R.string.reports_revenue), value = totalRevenue.formatCurrency(sym))
            SummaryCell(label = stringResource(R.string.reports_cost), value = totalCost.formatCurrency(sym))
            SummaryCell(
                label = stringResource(R.string.reports_profit),
                value = totalProfit.formatCurrency(sym),
                valueColor = if (totalProfit >= 0f) BrandPurple else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun ReportTableHeader(modifier: Modifier = Modifier) {
    // Start padding + WordSafeText: long headers (ES "Beneficio") keep a gap and shrink
    // rather than touching the next column or breaking mid-word.
    val numberHeaderStyle = MaterialTheme.typography.labelSmall.copy(color = TextMuted, textAlign = TextAlign.End)
    Row(modifier = modifier) {
        Text(
            text = stringResource(R.string.reports_col_piece),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.weight(2f)
        )
        WordSafeText(
            text = stringResource(R.string.reports_cost),
            style = numberHeaderStyle,
            modifier = Modifier.weight(1f).padding(start = 6.dp)
        )
        WordSafeText(
            text = stringResource(R.string.reports_col_price),
            style = numberHeaderStyle,
            modifier = Modifier.weight(1f).padding(start = 6.dp)
        )
        WordSafeText(
            text = stringResource(R.string.reports_profit),
            style = numberHeaderStyle,
            modifier = Modifier.weight(1f).padding(start = 6.dp)
        )
    }
}

@Composable
private fun ReportTableRow(row: PieceReportRow, modifier: Modifier = Modifier) {
    val sym = getCurrencySymbol(LocalContext.current)
    Row(
        modifier = modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = if (row.quantityTotal > 1) "${row.name} ×${row.quantityTotal}" else row.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                // Long names wrap to a second line and end with "…" instead of being cut silently
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = runCatching { WorkStatus.valueOf(row.status) }.getOrNull()
                    ?.let { stringResource(it.labelResId) } ?: row.status,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Text(
            text = row.materialCost?.formatCurrency(sym) ?: "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = (row.soldPrice ?: row.price)?.formatCurrency(sym) ?: "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        val profitColor = when {
            row.profit == null -> TextMuted
            row.profit >= 0f -> BrandPurple
            else -> MaterialTheme.colorScheme.error
        }
        Text(
            text = row.profit?.formatCurrency(sym) ?: "—",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = profitColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CustomerReportsContent(uiState: ReportsUiState) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SummaryCard(
                totalRevenue = uiState.totalRevenue,
                totalCost = uiState.totalCost,
                totalProfit = uiState.totalProfit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            CustomerTableHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        items(items = uiState.customerRows) { row ->
            CustomerTableRow(
                row = row,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun CustomerTableHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            text = stringResource(R.string.piece_field_sale_platform),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = stringResource(R.string.reports_revenue),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.reports_profit),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CustomerTableRow(row: CustomerReportRow, modifier: Modifier = Modifier) {
    val sym = getCurrencySymbol(LocalContext.current)
    Row(
        modifier = modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = row.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = pluralStringResource(R.plurals.reports_piece_count, row.pieceCount, row.pieceCount),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Text(
            text = row.revenue.formatCurrency(sym),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        val profitColor = when {
            row.profit >= 0f -> BrandPurple
            else -> MaterialTheme.colorScheme.error
        }
        Text(
            text = row.profit.formatCurrency(sym),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = profitColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
