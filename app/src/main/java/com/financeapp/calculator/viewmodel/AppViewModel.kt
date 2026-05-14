package com.financeapp.calculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.calculator.data.repository.AuthRepository
import com.financeapp.calculator.data.repository.SettingsRepository
import com.financeapp.calculator.data.repository.ThemeMode
import com.financeapp.calculator.utils.Outcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUiState(
    val bootCompleted: Boolean = false,
    val isAuthenticated: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        bootstrap()
        observeTheme()
    }

    private fun bootstrap() {
        viewModelScope.launch {
            val authenticated = if (authRepository.hasToken()) {
                authRepository.refreshSession() is Outcome.Success
            } else false
            _uiState.update { it.copy(isAuthenticated = authenticated, bootCompleted = true) }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun onAuthenticated() = _uiState.update { it.copy(isAuthenticated = true) }
    fun onSignedOut() = _uiState.update { it.copy(isAuthenticated = false) }
}
