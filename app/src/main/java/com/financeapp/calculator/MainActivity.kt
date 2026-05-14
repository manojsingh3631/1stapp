package com.financeapp.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.financeapp.calculator.ui.AppNavGraph
import com.financeapp.calculator.ui.theme.FinanceCalculatorTheme
import com.financeapp.calculator.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash visible until initial auth probe is done.
        splash.setKeepOnScreenCondition { !appViewModel.uiState.value.bootCompleted }

        setContent {
            val state by appViewModel.uiState.collectAsState()
            FinanceCalculatorTheme(themeMode = state.themeMode) {
                AppNavGraph(
                    isAuthenticated = state.isAuthenticated,
                    bootCompleted = state.bootCompleted
                )
            }
        }
    }
}
