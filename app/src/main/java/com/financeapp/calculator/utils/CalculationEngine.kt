package com.financeapp.calculator.utils

import com.financeapp.calculator.data.model.CalculationResult
import com.financeapp.calculator.data.model.CalculatorType
import com.financeapp.calculator.data.model.YearPoint
import kotlin.math.pow

/**
 * Pure, deterministic financial calculation engine.
 *
 * Mirrors the formulas used by the web app so results are consistent across platforms.
 * All rates are expressed as annual percentages (e.g. 12.0 means 12%).
 * All amounts are in the user's currency (typically INR).
 */
object CalculationEngine {

    // ----- SIP -----
    /**
     * Future value of a Systematic Investment Plan with monthly contributions [monthly]
     * at expected annual return [annualRatePct] over [years] years.
     *
     * Uses standard FV-of-annuity formula compounded monthly:
     *   FV = P * (((1 + r)^n - 1) / r) * (1 + r)
     *   where r = monthly rate, n = months, P = monthly investment, and the trailing (1 + r)
     *   accounts for the contribution being made at the start of each month.
     */
    fun sip(monthly: Double, annualRatePct: Double, years: Int): CalculationResult {
        require(monthly >= 0 && years > 0) { "Invalid SIP inputs" }
        val r = annualRatePct / 100.0 / 12.0
        val series = mutableListOf<YearPoint>()
        var balance = 0.0
        var invested = 0.0
        for (y in 1..years) {
            for (m in 1..12) {
                balance = (balance + monthly) * (1 + r)
                invested += monthly
            }
            series += YearPoint(y, invested, balance)
        }
        val gains = balance - invested
        return CalculationResult(
            type = CalculatorType.SIP,
            primary = balance,
            metrics = mapOf(
                "Total invested" to invested,
                "Estimated returns" to gains,
                "Maturity value" to balance
            ),
            series = series
        )
    }

    // ----- SWP -----
    /**
     * Systematic Withdrawal Plan: start with [principal], withdraw [monthlyWithdrawal]
     * at the end of each month, while the remaining corpus earns [annualRatePct] p.a.
     * Stops at the requested [years] or when corpus depletes, whichever is first.
     */
    fun swp(principal: Double, monthlyWithdrawal: Double, annualRatePct: Double, years: Int): CalculationResult {
        require(principal >= 0 && monthlyWithdrawal >= 0 && years > 0) { "Invalid SWP inputs" }
        val r = annualRatePct / 100.0 / 12.0
        var balance = principal
        var totalWithdrawn = 0.0
        val series = mutableListOf<YearPoint>()
        var depleted = false
        for (y in 1..years) {
            for (m in 1..12) {
                if (balance <= 0.0) { depleted = true; break }
                balance *= (1 + r)
                val withdraw = minOf(monthlyWithdrawal, balance)
                balance -= withdraw
                totalWithdrawn += withdraw
            }
            series += YearPoint(y, totalWithdrawn, maxOf(0.0, balance))
            if (depleted) break
        }
        return CalculationResult(
            type = CalculatorType.SWP,
            primary = maxOf(0.0, balance),
            metrics = mapOf(
                "Total withdrawn" to totalWithdrawn,
                "Final balance" to maxOf(0.0, balance),
                "Months sustained" to series.size.toDouble() * 12.0
            ),
            series = series
        )
    }

    // ----- Lumpsum -----
    /** Future value of a one-time investment compounded annually. */
    fun lumpsum(principal: Double, annualRatePct: Double, years: Int): CalculationResult {
        require(principal >= 0 && years > 0) { "Invalid Lumpsum inputs" }
        val r = annualRatePct / 100.0
        val series = mutableListOf<YearPoint>()
        for (y in 1..years) {
            val value = principal * (1 + r).pow(y)
            series += YearPoint(y, principal, value)
        }
        val maturity = series.last().value
        return CalculationResult(
            type = CalculatorType.LUMPSUM,
            primary = maturity,
            metrics = mapOf(
                "Principal" to principal,
                "Estimated returns" to (maturity - principal),
                "Maturity value" to maturity
            ),
            series = series
        )
    }

    // ----- EMI -----
    /**
     * Equated Monthly Installment for principal [principal] at [annualRatePct] over [years] years.
     *   EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     */
    fun emi(principal: Double, annualRatePct: Double, years: Int): CalculationResult {
        require(principal > 0 && years > 0) { "Invalid EMI inputs" }
        val r = annualRatePct / 100.0 / 12.0
        val n = years * 12
        val emi = if (r == 0.0) principal / n
        else principal * r * (1 + r).pow(n) / ((1 + r).pow(n) - 1)
        val totalPayment = emi * n
        val totalInterest = totalPayment - principal

        // Year-by-year outstanding balance after amortization
        val series = mutableListOf<YearPoint>()
        var outstanding = principal
        var paid = 0.0
        for (y in 1..years) {
            for (m in 1..12) {
                val interest = outstanding * r
                val principalPart = emi - interest
                outstanding = maxOf(0.0, outstanding - principalPart)
                paid += emi
            }
            series += YearPoint(y, paid, outstanding)
        }
        return CalculationResult(
            type = CalculatorType.EMI,
            primary = emi,
            metrics = mapOf(
                "Monthly EMI" to emi,
                "Total interest" to totalInterest,
                "Total payment" to totalPayment
            ),
            series = series
        )
    }

    // ----- ELSS -----
    /**
     * ELSS calculation: same growth math as SIP, plus a tax-saving headline (Sec 80C).
     * Assumes [taxSlabPct] is the user's marginal slab; tax saved per year is
     * min(annual investment, 150000) * slab%.
     */
    fun elss(monthly: Double, annualRatePct: Double, years: Int, taxSlabPct: Double): CalculationResult {
        val growth = sip(monthly, annualRatePct, years)
        val annualInvestment = monthly * 12
        val deductible = minOf(annualInvestment, 150_000.0)
        val taxSavedPerYear = deductible * (taxSlabPct / 100.0)
        val taxSavedTotal = taxSavedPerYear * years
        return CalculationResult(
            type = CalculatorType.ELSS,
            primary = growth.primary,
            metrics = growth.metrics + mapOf(
                "Annual tax saved" to taxSavedPerYear,
                "Total tax saved" to taxSavedTotal
            ),
            series = growth.series
        )
    }

    // ----- PPF -----
    /**
     * Public Provident Fund: yearly deposit [yearly] at the start of the year, compounded
     * annually at [annualRatePct]. Standard PPF tenure is 15 years; engine accepts any.
     */
    fun ppf(yearly: Double, annualRatePct: Double, years: Int): CalculationResult {
        require(yearly >= 0 && years > 0) { "Invalid PPF inputs" }
        val r = annualRatePct / 100.0
        var balance = 0.0
        var invested = 0.0
        val series = mutableListOf<YearPoint>()
        for (y in 1..years) {
            // Deposit at start of year, interest credited at year end on the running balance.
            balance += yearly
            invested += yearly
            balance *= (1 + r)
            series += YearPoint(y, invested, balance)
        }
        return CalculationResult(
            type = CalculatorType.PPF,
            primary = balance,
            metrics = mapOf(
                "Total invested" to invested,
                "Total interest" to (balance - invested),
                "Maturity value" to balance
            ),
            series = series
        )
    }
}
