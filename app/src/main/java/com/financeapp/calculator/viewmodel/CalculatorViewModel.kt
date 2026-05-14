package com.financeapp.calculator.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.calculator.data.api.dto.CalculationCreate
import com.financeapp.calculator.data.model.CalculationResult
import com.financeapp.calculator.data.model.CalculatorType
import com.financeapp.calculator.data.repository.AuthRepository
import com.financeapp.calculator.data.repository.CalculationRepository
import com.financeapp.calculator.utils.CalculationEngine
import com.financeapp.calculator.utils.InputValidator
import com.financeapp.calculator.utils.Outcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Inputs for any calculator. Not all fields are used by every calc — see [CalculatorUiState.type]. */
data class CalculatorInputs(
    val amount: String = "",
    val rate: String = "",
    val years: String = "",
    val monthlyWithdrawal: String = "",
    val taxSlab: String = "30"
)

data class CalculatorUiState(
    val type: CalculatorType,
    val inputs: CalculatorInputs = CalculatorInputs(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val result: CalculationResult? = null,
    val saving: Boolean = false,
    val savedMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calculationRepository: CalculationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val type: CalculatorType =
        CalculatorType.fromKey(savedStateHandle.get<String>("type") ?: "sip") ?: CalculatorType.SIP

    private val _state = MutableStateFlow(CalculatorUiState(type = type))
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    fun update(transform: (CalculatorInputs) -> CalculatorInputs) {
        _state.update { it.copy(inputs = transform(it.inputs), fieldErrors = emptyMap()) }
    }

    fun calculate() {
        val errors = validate()
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors, result = null) }
            return
        }
        val i = _state.value.inputs
        val amount = i.amount.toDouble()
        val rate = i.rate.toDouble()
        val years = i.years.toInt()
        val result = runCatching {
            when (type) {
                CalculatorType.SIP -> CalculationEngine.sip(amount, rate, years)
                CalculatorType.SWP -> CalculationEngine.swp(amount, i.monthlyWithdrawal.toDouble(), rate, years)
                CalculatorType.LUMPSUM -> CalculationEngine.lumpsum(amount, rate, years)
                CalculatorType.EMI -> CalculationEngine.emi(amount, rate, years)
                CalculatorType.ELSS -> CalculationEngine.elss(amount, rate, years, i.taxSlab.toDouble())
                CalculatorType.PPF -> CalculationEngine.ppf(amount, rate, years)
            }
        }.getOrElse {
            _state.update { s -> s.copy(errorMessage = it.message ?: "Calculation failed.") }
            return
        }
        _state.update { it.copy(result = result, errorMessage = null) }
    }

    fun save(savedName: String?) {
        val result = _state.value.result ?: return
        if (authRepository.currentUser.value == null) {
            _state.update { it.copy(errorMessage = "Please sign in to save calculations.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val payload = CalculationCreate(
                type = type.key,
                inputs = inputsToMap(),
                results = resultToMap(result),
                savedName = savedName?.takeIf { it.isNotBlank() }
            )
            when (val outcome = calculationRepository.save(payload)) {
                is Outcome.Success -> _state.update {
                    it.copy(saving = false, savedMessage = "Saved.")
                }
                is Outcome.Failure -> _state.update {
                    it.copy(saving = false, errorMessage = outcome.message)
                }
                Outcome.Loading -> {}
            }
        }
    }

    fun consumeMessages() = _state.update { it.copy(savedMessage = null, errorMessage = null) }

    private fun validate(): Map<String, String> {
        val i = _state.value.inputs
        val errors = mutableMapOf<String, String>()
        InputValidator.positiveAmount(i.amount, fieldLabel("amount"))?.let { errors["amount"] = it }
        InputValidator.rate(i.rate)?.let { errors["rate"] = it }
        InputValidator.years(i.years)?.let { errors["years"] = it }
        if (type == CalculatorType.SWP) {
            InputValidator.positiveAmount(i.monthlyWithdrawal, "Monthly withdrawal")?.let {
                errors["monthlyWithdrawal"] = it
            }
        }
        if (type == CalculatorType.ELSS) {
            InputValidator.rate(i.taxSlab, "Tax slab")?.let { errors["taxSlab"] = it }
        }
        return errors
    }

    private fun fieldLabel(name: String): String = when {
        type == CalculatorType.SIP && name == "amount" -> "Monthly investment"
        type == CalculatorType.LUMPSUM && name == "amount" -> "Investment amount"
        type == CalculatorType.SWP && name == "amount" -> "Total investment"
        type == CalculatorType.EMI && name == "amount" -> "Loan amount"
        type == CalculatorType.ELSS && name == "amount" -> "Monthly investment"
        type == CalculatorType.PPF && name == "amount" -> "Yearly investment"
        else -> "Amount"
    }

    private fun inputsToMap(): Map<String, Any> {
        val i = _state.value.inputs
        val base = mutableMapOf<String, Any>(
            "amount" to i.amount.toDouble(),
            "rate" to i.rate.toDouble(),
            "years" to i.years.toInt()
        )
        if (type == CalculatorType.SWP) base["monthly_withdrawal"] = i.monthlyWithdrawal.toDouble()
        if (type == CalculatorType.ELSS) base["tax_slab"] = i.taxSlab.toDouble()
        return base
    }

    private fun resultToMap(r: CalculationResult): Map<String, Any> = mapOf(
        "primary" to r.primary,
        "metrics" to r.metrics,
        "series" to r.series.map { mapOf("year" to it.year, "invested" to it.invested, "value" to it.value) }
    )
}
