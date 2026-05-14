package com.financeapp.calculator.data.model

enum class CalculatorType(val key: String, val displayName: String) {
    SIP("sip", "SIP"),
    SWP("swp", "SWP"),
    LUMPSUM("lumpsum", "Lumpsum"),
    EMI("emi", "EMI"),
    ELSS("elss", "ELSS"),
    PPF("ppf", "PPF");

    companion object {
        fun fromKey(key: String): CalculatorType? = values().firstOrNull { it.key == key }
    }
}

/** Year-by-year growth point used to render line/bar charts. */
data class YearPoint(
    val year: Int,
    val invested: Double,
    val value: Double
)

data class CalculationResult(
    val type: CalculatorType,
    /** The single headline number, e.g. maturity value or EMI per month. */
    val primary: Double,
    /** Map of additional labeled metrics, e.g. "Total invested" -> 120000.0 */
    val metrics: Map<String, Double>,
    val series: List<YearPoint> = emptyList()
)
