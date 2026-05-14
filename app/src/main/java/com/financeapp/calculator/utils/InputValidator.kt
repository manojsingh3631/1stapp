package com.financeapp.calculator.utils

object InputValidator {

    /** Returns null if valid, otherwise an error message. */
    fun positiveAmount(raw: String, field: String = "Amount"): String? {
        val value = raw.toDoubleOrNull() ?: return "$field must be a number."
        if (value <= 0.0) return "$field must be greater than zero."
        if (value > 1e12) return "$field is too large."
        return null
    }

    fun nonNegativeAmount(raw: String, field: String = "Amount"): String? {
        val value = raw.toDoubleOrNull() ?: return "$field must be a number."
        if (value < 0.0) return "$field cannot be negative."
        return null
    }

    fun rate(raw: String, field: String = "Rate"): String? {
        val value = raw.toDoubleOrNull() ?: return "$field must be a number."
        if (value < 0.0 || value > 100.0) return "$field must be between 0 and 100."
        return null
    }

    fun years(raw: String, field: String = "Duration", maxYears: Int = 50): String? {
        val value = raw.toIntOrNull() ?: return "$field must be a whole number of years."
        if (value < 1 || value > maxYears) return "$field must be between 1 and $maxYears years."
        return null
    }
}
