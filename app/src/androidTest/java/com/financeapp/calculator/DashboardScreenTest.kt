package com.financeapp.calculator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.financeapp.calculator.data.model.CalculatorType
import com.financeapp.calculator.ui.screens.dashboard.DashboardScreen
import com.financeapp.calculator.ui.theme.FinanceCalculatorTheme
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun dashboardShowsAllCalculatorTiles() {
        composeRule.setContent {
            FinanceCalculatorTheme {
                DashboardScreen(
                    isAuthenticated = false,
                    onOpenCalculator = {},
                    onOpenHistory = {},
                    onOpenSettings = {},
                    onOpenLogin = {}
                )
            }
        }
        CalculatorType.values().forEach { type ->
            composeRule.onNodeWithText(type.displayName).assertIsDisplayed()
        }
    }
}
