package com.financeapp.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    suffix: String? = null,
    decimal: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { raw ->
                // strip anything that isn't a digit or a single '.'
                val filtered = if (decimal) raw.filter { it.isDigit() || it == '.' }
                else raw.filter { it.isDigit() }
                // enforce at most one '.'
                val normalized = if (decimal && filtered.count { it == '.' } > 1) {
                    val first = filtered.indexOf('.')
                    filtered.substring(0, first + 1) + filtered.substring(first + 1).filter { it != '.' }
                } else filtered
                onValueChange(normalized)
            },
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            suffix = suffix?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
