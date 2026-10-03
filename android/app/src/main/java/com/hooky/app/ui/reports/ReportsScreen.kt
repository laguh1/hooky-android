package com.hooky.app.ui.reports

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

@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Cost & Profit Report",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                val hasData = if (selectedTab == ReportTab.PIECES) uiState.rows.isNotEmpty() else uiState.customerRows.isNotEmpty()
                if (uiState.isPremium && hasData) {
                    IconButton(onClick = {
                        val csv = if (selectedTab == ReportTab.PIECES) viewModel.buildCsv() else viewModel.buildCustomerCsv()
                        val subject = if (selectedTab == ReportTab.PIECES) "Hooky Cost & Profit Report" else "Hooky Revenue by Customer Report"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_TEXT, csv)
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                        }
                        context.startActivity(Intent.createChooser(intent, "Export CSV"))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Export CSV",
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
                    PremiumGate()
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
                selectedContainerColor = Slate,
                selectedLabelColor = androidx.compose.ui.graphics.Color.White
            )
        )
        FilterChip(
            selected = selectedTab == ReportTab.CUSTOMERS,
            onClick = { onTabSelected(ReportTab.CUSTOMERS) },
            label = { Text(stringResource(R.string.reports_tab_customers)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Slate,
                selectedLabelColor = androidx.compose.ui.graphics.Color.White
            )
        )
    }
}

@Composable
private fun PremiumGate() {
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
                text = "Premium Feature",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Cost & profit reports are available for premium testers. Contact us to get early access.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
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
            SummaryCell(label = "Revenue", value = totalRevenue.formatCurrency(sym))
            SummaryCell(label = "Cost", value = totalCost.formatCurrency(sym))
            SummaryCell(
                label = "Profit",
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
    Row(modifier = modifier) {
        Text(
            text = "Piece",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "Cost",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Price",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Profit",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
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
                maxLines = 1
            )
            Text(
                text = row.status.lowercase().replace('_', ' '),
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
            text = "Revenue",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Profit",
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
                text = if (row.pieceCount == 1) "1 piece" else "${row.pieceCount} pieces",
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
