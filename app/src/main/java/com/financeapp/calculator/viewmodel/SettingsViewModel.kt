package com.financeapp.calculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.calculator.data.model.User
import com.financeapp.calculator.data.repository.AuthRepository
import com.financeapp.calculator.data.repository.SettingsRepository
import com.financeapp.calculator.data.repository.ThemeMode
import com.financeapp.calculator.utils.GoogleAuthClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val currentUser: StateFlow<User?> = authRepository.currentUser

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setTheme(mode) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            googleAuthClient.signOut()
            onDone()
        }
    }
}
