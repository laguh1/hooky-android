package com.crochet.manager.ui.calculator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PriceBreakdown(
    val materialCost: Float,
    val laborCost: Float,
    val subtotal: Float,
    val complexityAdjustment: Float,
    val profitAmount: Float,
    val suggestedPrice: Float
)

data class CalculatorUiState(
    val materialCost: String = "",
    val workHours: String = "",
    val laborRate: String = "8.00",
    val complexityFactor: Float = 1.0f,
    val profitMargin: Float = 0.20f,
    val suggestedPrice: Float? = null,
    val breakdown: PriceBreakdown? = null
)

sealed interface CalculatorAction {
    data class MaterialCostChanged(val value: String) : CalculatorAction
    data class WorkHoursChanged(val value: String) : CalculatorAction
    data class LaborRateChanged(val value: String) : CalculatorAction
    data class ComplexityFactorChanged(val factor: Float) : CalculatorAction
    data class ProfitMarginChanged(val margin: Float) : CalculatorAction
}

@HiltViewModel
class PriceCalculatorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.MaterialCostChanged -> {
                _uiState.update { it.copy(materialCost = action.value) }
            }
            is CalculatorAction.WorkHoursChanged -> {
                _uiState.update { it.copy(workHours = action.value) }
            }
            is CalculatorAction.LaborRateChanged -> {
                _uiState.update { it.copy(laborRate = action.value) }
            }
            is CalculatorAction.ComplexityFactorChanged -> {
                _uiState.update { it.copy(complexityFactor = action.factor) }
            }
            is CalculatorAction.ProfitMarginChanged -> {
                _uiState.update { it.copy(profitMargin = action.margin) }
            }
        }
        recalculate()
    }

    private fun recalculate() {
        val current = _uiState.value
        val materialCost = current.materialCost.toFloatOrNull() ?: return clearResult()
        val workHours = current.workHours.toFloatOrNull() ?: return clearResult()
        val laborRate = current.laborRate.toFloatOrNull() ?: return clearResult()
        if (materialCost < 0f || workHours < 0f || laborRate < 0f) return clearResult()

        val laborCost = workHours * laborRate
        val subtotal = materialCost + laborCost
        val withComplexity = subtotal * current.complexityFactor
        val profitAmount = withComplexity * current.profitMargin
        val suggestedPrice = withComplexity * (1f + current.profitMargin)

        val breakdown = PriceBreakdown(
            materialCost = materialCost,
            laborCost = laborCost,
            subtotal = subtotal,
            complexityAdjustment = withComplexity - subtotal,
            profitAmount = profitAmount,
            suggestedPrice = suggestedPrice
        )

        _uiState.update {
            it.copy(
                suggestedPrice = suggestedPrice,
                breakdown = breakdown
            )
        }
    }

    private fun clearResult() {
        _uiState.update { it.copy(suggestedPrice = null, breakdown = null) }
    }
}
