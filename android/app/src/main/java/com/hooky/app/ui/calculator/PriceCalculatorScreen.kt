package com.hooky.app.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.hooky.app.ui.settings.formatCurrency
import com.hooky.app.ui.settings.getCurrencySymbol
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hooky.app.R
import com.hooky.app.ui.theme.BackgroundLight
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.BorderStrong
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White

private data class ComplexityOption(
    val label: String,
    val factor: Float
)

private val complexityOptions = listOf(
    ComplexityOption("Normal ×1.0", 1.0f),
    ComplexityOption("Complex ×1.2", 1.2f),
    ComplexityOption("Very Complex ×1.5", 1.5f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: PriceCalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val currencySymbol = getCurrencySymbol(LocalContext.current)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.calculator_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Input card ----
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BackgroundLight,
                border = BorderStroke(1.dp, BorderLight),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Inputs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Material cost
                    LabeledField(label = "Material cost ($currencySymbol)") {
                        OutlinedTextField(
                            value = uiState.materialCost,
                            onValueChange = { viewModel.onAction(CalculatorAction.MaterialCostChanged(it)) },
                            placeholder = { Text("0.00", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedBorderColor = BrandPurple,
                                cursorColor = BrandPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Work hours
                    LabeledField(label = "Work hours (h)") {
                        OutlinedTextField(
                            value = uiState.workHours,
                            onValueChange = { viewModel.onAction(CalculatorAction.WorkHoursChanged(it)) },
                            placeholder = { Text("0.0", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedBorderColor = BrandPurple,
                                cursorColor = BrandPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Labor rate
                    LabeledField(label = "Labor rate ($currencySymbol/hr)") {
                        OutlinedTextField(
                            value = uiState.laborRate,
                            onValueChange = { viewModel.onAction(CalculatorAction.LaborRateChanged(it)) },
                            placeholder = { Text("8.00", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedBorderColor = BrandPurple,
                                cursorColor = BrandPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(color = BorderLight, thickness = 1.dp)

                    // Complexity factor
                    Text(
                        text = stringResource(R.string.calculator_complexity),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        complexityOptions.forEach { option ->
                            val selected = uiState.complexityFactor == option.factor
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onAction(CalculatorAction.ComplexityFactorChanged(option.factor)) },
                                label = {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate,
                                    selectedLabelColor = White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = BorderLight,
                                    selectedBorderColor = Slate,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.dp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Divider(color = BorderLight, thickness = 1.dp)

                    // Profit margin slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Profit margin",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(uiState.profitMargin * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate
                        )
                    }
                    Slider(
                        value = uiState.profitMargin,
                        onValueChange = { viewModel.onAction(CalculatorAction.ProfitMarginChanged(it)) },
                        valueRange = 0f..0.5f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Slate,
                            activeTrackColor = Slate,
                            inactiveTrackColor = BorderStrong
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ---- Result card ----
            AnimatedVisibility(
                visible = uiState.suggestedPrice != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                uiState.suggestedPrice?.let { price ->
                    ResultCard(
                        suggestedPrice = price,
                        breakdown = uiState.breakdown,
                        workHours = uiState.workHours.toFloatOrNull() ?: 0f,
                        laborRate = uiState.laborRate.toFloatOrNull() ?: 8f,
                        complexityFactor = uiState.complexityFactor,
                        profitMargin = uiState.profitMargin,
                        currencySymbol = currencySymbol,
                        onCopyPrice = {
                            clipboardManager.setText(AnnotatedString(price.formatCurrency(currencySymbol)))
                        }
                    )
                }
            }

            // Placeholder card when no result yet
            AnimatedVisibility(
                visible = uiState.suggestedPrice == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, BorderLight),
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = "Fill in cost and hours to see the suggested price",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Result card
// ---------------------------------------------------------------------------

@Composable
private fun ResultCard(
    suggestedPrice: Float,
    breakdown: PriceBreakdown?,
    workHours: Float,
    laborRate: Float,
    complexityFactor: Float,
    profitMargin: Float,
    currencySymbol: String,
    onCopyPrice: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Slate,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Price header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.calculator_suggested_price),
                        style = MaterialTheme.typography.labelMedium,
                        color = White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = suggestedPrice.formatCurrency(currencySymbol),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
                Button(
                    onClick = onCopyPrice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White.copy(alpha = 0.15f),
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_copy),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Breakdown
            AnimatedVisibility(
                visible = breakdown != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                breakdown?.let {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Divider(color = White.copy(alpha = 0.2f), thickness = 1.dp)
                        BreakdownRow(
                            label = "Materials",
                            value = it.materialCost.formatCurrency(currencySymbol)
                        )
                        BreakdownRow(
                            label = "Labor (${formatHours(workHours)} × ${laborRate.formatCurrency(currencySymbol)})",
                            value = it.laborCost.formatCurrency(currencySymbol)
                        )
                        BreakdownRow(
                            label = "Complexity (×${"%.1f".format(complexityFactor)})",
                            value = if (it.complexityAdjustment != 0f) "+${it.complexityAdjustment.formatCurrency(currencySymbol)}" else "—"
                        )
                        BreakdownRow(
                            label = "Profit (${(profitMargin * 100).toInt()}%)",
                            value = it.profitAmount.formatCurrency(currencySymbol)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = White
        )
    }
}

// ---------------------------------------------------------------------------
// Labeled field wrapper
// ---------------------------------------------------------------------------

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        content()
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatHours(hours: Float): String =
    if (hours % 1f == 0f) "${hours.toInt()}h" else "${hours}h"
