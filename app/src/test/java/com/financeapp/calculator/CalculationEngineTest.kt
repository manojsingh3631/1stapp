package com.financeapp.calculator

import com.financeapp.calculator.data.model.CalculatorType
import com.financeapp.calculator.utils.CalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for [CalculationEngine].
 *
 * These tests check formula correctness against hand-derived values and
 * a few invariants (e.g. SIP balance > total invested when rate > 0).
 */
class CalculationEngineTest {

    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 1.0) {
        assertTrue(
            "Expected $expected, got $actual (diff ${abs(expected - actual)})",
            abs(expected - actual) < tolerance
        )
    }

    // ---------- SIP ----------
    @Test
    fun sip_grows_above_invested_when_rate_positive() {
        val r = CalculationEngine.sip(monthly = 10_000.0, annualRatePct = 12.0, years = 10)
        val invested = r.metrics["Total invested"]!!
        assertEquals(1_200_000.0, invested, 0.01)
        assertTrue("FV must exceed invested when rate > 0", r.primary > invested)
        assertEquals(10, r.series.size)
    }

    @Test
    fun sip_zero_rate_equals_total_invested() {
        val r = CalculationEngine.sip(monthly = 5_000.0, annualRatePct = 0.0, years = 5)
        assertNear(5_000.0 * 12 * 5, r.primary, 0.01)
    }

    // ---------- SWP ----------
    @Test
    fun swp_balance_decreases_or_stable_with_zero_growth() {
        val r = CalculationEngine.swp(
            principal = 1_000_000.0,
            monthlyWithdrawal = 10_000.0,
            annualRatePct = 0.0,
            years = 5
        )
        // 60 withdrawals * 10000 = 600000 withdrawn, 400000 left.
        assertNear(600_000.0, r.metrics["Total withdrawn"]!!, 1.0)
        assertNear(400_000.0, r.primary, 1.0)
    }

    @Test
    fun swp_depletes_if_withdrawals_outpace_growth() {
        val r = CalculationEngine.swp(
            principal = 100_000.0,
            monthlyWithdrawal = 20_000.0,
            annualRatePct = 6.0,
            years = 10
        )
        assertTrue("Should deplete before 10 years", r.primary <= 1.0)
    }

    // ---------- Lumpsum ----------
    @Test
    fun lumpsum_matches_compound_interest_formula() {
        // 100000 * 1.10^5 = 161051.0
        val r = CalculationEngine.lumpsum(principal = 100_000.0, annualRatePct = 10.0, years = 5)
        assertNear(161_051.0, r.primary, 0.5)
    }

    // ---------- EMI ----------
    @Test
    fun emi_matches_known_value() {
        // 10L @ 9% for 20yr ≈ ₹8,997.26/mo
        val r = CalculationEngine.emi(principal = 1_000_000.0, annualRatePct = 9.0, years = 20)
        assertNear(8_997.26, r.primary, 1.0)
    }

    @Test
    fun emi_outstanding_reaches_zero_at_end() {
        val r = CalculationEngine.emi(principal = 500_000.0, annualRatePct = 12.0, years = 10)
        val finalOutstanding = r.series.last().value
        assertTrue("Loan should be fully paid off", finalOutstanding < 1.0)
    }

    // ---------- ELSS ----------
    @Test
    fun elss_includes_tax_savings_metric() {
        val r = CalculationEngine.elss(
            monthly = 12_500.0,    // 150,000 / year — at the 80C cap
            annualRatePct = 12.0,
            years = 3,
            taxSlabPct = 30.0
        )
        val annualSaved = r.metrics["Annual tax saved"]!!
        assertNear(45_000.0, annualSaved, 0.01) // 150000 * 30%
        assertEquals(CalculatorType.ELSS, r.type)
    }

    @Test
    fun elss_caps_deduction_at_150k() {
        val r = CalculationEngine.elss(
            monthly = 20_000.0,    // 240,000 / year — exceeds cap
            annualRatePct = 12.0,
            years = 1,
            taxSlabPct = 30.0
        )
        // Capped at 150000 deductible, not 240000
        assertNear(45_000.0, r.metrics["Annual tax saved"]!!, 0.01)
    }

    // ---------- PPF ----------
    @Test
    fun ppf_first_year_balance_is_yearly_times_one_plus_rate() {
        val r = CalculationEngine.ppf(yearly = 100_000.0, annualRatePct = 7.1, years = 15)
        assertNear(107_100.0, r.series.first().value, 0.5)
    }

    @Test
    fun ppf_total_invested_is_yearly_times_years() {
        val r = CalculationEngine.ppf(yearly = 50_000.0, annualRatePct = 7.1, years = 15)
        assertNear(750_000.0, r.metrics["Total invested"]!!, 0.5)
    }
}
