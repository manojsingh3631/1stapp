package com.financeapp.calculator.ui.screens.calculators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.calculator.data.model.CalculatorType
import com.financeapp.calculator.ui.components.GrowthChart
import com.financeapp.calculator.ui.components.NumberField
import com.financeapp.calculator.ui.components.ResultCard
import com.financeapp.calculator.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    isAuthenticated: Boolean,
    onBack: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedMessage, state.errorMessage) {
        state.savedMessage?.let { snackbar.showSnackbar(it) }
        state.errorMessage?.let { snackbar.showSnackbar(it) }
        viewModel.consumeMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.type.displayName} Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InputBlock(state = state, onChange = viewModel::update)

            Button(
                onClick = viewModel::calculate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Text("  Calculate")
            }

            state.result?.let { r ->
                ResultCard(result = r)
                Text("Growth over time", style = MaterialTheme.typography.titleLarge)
                GrowthChart(series = r.series)

                if (isAuthenticated) {
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Text("  Save calculation")
                    }
                } else {
                    Text(
                        "Sign in to save and revisit your calculations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    if (showSaveDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save calculation") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    viewModel.save(name)
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun InputBlock(
    state: com.financeapp.calculator.viewmodel.CalculatorUiState,
    onChange: ((com.financeapp.calculator.viewmodel.CalculatorInputs) -> com.financeapp.calculator.viewmodel.CalculatorInputs) -> Unit
) {
    val labels = labelsFor(state.type)
    NumberField(
        label = labels.amount,
        value = state.inputs.amount,
        onValueChange = { v -> onChange { it.copy(amount = v) } },
        error = state.fieldErrors["amount"],
        suffix = "₹"
    )
    if (state.type == CalculatorType.SWP) {
        NumberField(
            label = "Monthly withdrawal",
            value = state.inputs.monthlyWithdrawal,
            onValueChange = { v -> onChange { it.copy(monthlyWithdrawal = v) } },
            error = state.fieldErrors["monthlyWithdrawal"],
            suffix = "₹"
        )
    }
    NumberField(
        label = labels.rate,
        value = state.inputs.rate,
        onValueChange = { v -> onChange { it.copy(rate = v) } },
        error = state.fieldErrors["rate"],
        suffix = "%"
    )
    NumberField(
        label = "Duration (years)",
        value = state.inputs.years,
        onValueChange = { v -> onChange { it.copy(years = v) } },
        error = state.fieldErrors["years"],
        decimal = false,
        suffix = "yrs"
    )
    if (state.type == CalculatorType.ELSS) {
        NumberField(
            label = "Tax slab",
            value = state.inputs.taxSlab,
            onValueChange = { v -> onChange { it.copy(taxSlab = v) } },
            error = state.fieldErrors["taxSlab"],
            suffix = "%"
        )
    }
}

private data class Labels(val amount: String, val rate: String)

private fun labelsFor(type: CalculatorType): Labels = when (type) {
    CalculatorType.SIP -> Labels("Monthly investment", "Expected return rate (p.a.)")
    CalculatorType.LUMPSUM -> Labels("Investment amount", "Expected return rate (p.a.)")
    CalculatorType.SWP -> Labels("Total investment", "Expected return rate (p.a.)")
    CalculatorType.EMI -> Labels("Loan amount", "Interest rate (p.a.)")
    CalculatorType.ELSS -> Labels("Monthly investment", "Expected return rate (p.a.)")
    CalculatorType.PPF -> Labels("Yearly investment", "Interest rate (p.a.)")
}
