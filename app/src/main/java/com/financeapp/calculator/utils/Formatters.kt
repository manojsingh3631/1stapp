package com.financeapp.calculator.utils

import java.text.NumberFormat
import java.util.Locale

object Formatters {
    private val inr: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    private val plain: NumberFormat = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
    }

    fun currency(value: Double): String = inr.format(value)
    fun number(value: Double): String = plain.format(value)
    fun percent(value: Double): String = "${"%.2f".format(value)}%"
}
