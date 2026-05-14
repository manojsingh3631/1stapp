package com.financeapp.calculator.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.calculator.data.repository.AuthRepository
import com.financeapp.calculator.utils.GoogleAuthClient
import com.financeapp.calculator.utils.Outcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val signedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun handleSignInResult(data: Intent?) {
        val idToken = googleAuthClient.idTokenFromResult(data)
        if (idToken.isNullOrBlank()) {
            _state.update { it.copy(error = "Google sign-in was cancelled or failed.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val outcome = authRepository.signInWithGoogle(idToken)) {
                is Outcome.Success -> _state.update { it.copy(loading = false, signedIn = true) }
                is Outcome.Failure -> _state.update { it.copy(loading = false, error = outcome.message) }
                Outcome.Loading -> {}
            }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }
}
