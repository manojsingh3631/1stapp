package com.financeapp.calculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.calculator.data.local.entity.CalculationEntity
import com.financeapp.calculator.data.repository.AuthRepository
import com.financeapp.calculator.data.repository.CalculationRepository
import com.financeapp.calculator.utils.Outcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val refreshing: Boolean = false,
    val errorMessage: String? = null,
    val query: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val calculationRepository: CalculationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(HistoryUiState())
    val ui: StateFlow<HistoryUiState> = _ui.asStateFlow()

    val items: StateFlow<List<CalculationEntity>> =
        authRepository.currentUser
            .flatMapLatest { user ->
                if (user == null) flowOf(emptyList()) else calculationRepository.observeForUser(user.id)
            }
            .combine(_ui) { list, ui ->
                if (ui.query.isBlank()) list
                else list.filter {
                    it.type.contains(ui.query, ignoreCase = true) ||
                            (it.savedName?.contains(ui.query, ignoreCase = true) ?: false)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun setQuery(q: String) = _ui.update { it.copy(query = q) }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(refreshing = true, errorMessage = null) }
            when (val outcome = calculationRepository.refreshFromServer()) {
                is Outcome.Success -> _ui.update { it.copy(refreshing = false) }
                is Outcome.Failure -> _ui.update { it.copy(refreshing = false, errorMessage = outcome.message) }
                Outcome.Loading -> {}
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            when (val outcome = calculationRepository.delete(id)) {
                is Outcome.Failure -> _ui.update { it.copy(errorMessage = outcome.message) }
                else -> {}
            }
        }
    }

    fun consumeError() = _ui.update { it.copy(errorMessage = null) }
}
