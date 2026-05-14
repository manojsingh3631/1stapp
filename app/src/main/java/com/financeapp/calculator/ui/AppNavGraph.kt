package com.financeapp.calculator.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.financeapp.calculator.ui.screens.calculators.CalculatorScreen
import com.financeapp.calculator.ui.screens.dashboard.DashboardScreen
import com.financeapp.calculator.ui.screens.history.HistoryScreen
import com.financeapp.calculator.ui.screens.login.LoginScreen
import com.financeapp.calculator.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(
    isAuthenticated: Boolean,
    bootCompleted: Boolean
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destinations.DASHBOARD) {
        composable(Destinations.LOGIN) {
            LoginScreen(
                onAuthenticated = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onContinueAsGuest = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                isAuthenticated = isAuthenticated,
                onOpenCalculator = { type -> navController.navigate(Destinations.calculator(type.key)) },
                onOpenHistory = { navController.navigate(Destinations.HISTORY) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
                onOpenLogin = { navController.navigate(Destinations.LOGIN) }
            )
        }

        composable(
            route = Destinations.CALCULATOR,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) {
            CalculatorScreen(
                isAuthenticated = isAuthenticated,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(0)
                    }
                }
            )
        }
    }

    // Guest mode is a valid path — the Dashboard exposes a "Sign in" CTA when unauthenticated,
    // so we don't force-redirect anyone away from the start destination.
    // bootCompleted is consumed by the splash gate in MainActivity, not here.
    @Suppress("UNUSED_PARAMETER") val _ignored = bootCompleted
}
