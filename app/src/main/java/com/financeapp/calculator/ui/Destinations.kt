package com.financeapp.calculator.ui

object Destinations {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    /** Calculator screen with required {type} key (sip|swp|lumpsum|emi|elss|ppf). */
    const val CALCULATOR = "calculator/{type}"
    fun calculator(type: String) = "calculator/$type"
}
