package com.financeapp.calculator.data.repository

import com.financeapp.calculator.data.api.FinanceApi
import com.financeapp.calculator.data.api.dto.SessionRequest
import com.financeapp.calculator.data.model.User
import com.financeapp.calculator.utils.Outcome
import com.financeapp.calculator.utils.SecureTokenStore
import com.financeapp.calculator.utils.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: FinanceApi,
    private val tokenStore: SecureTokenStore
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun hasToken(): Boolean = !tokenStore.getToken().isNullOrBlank()

    /** Exchange a Google ID token for a backend session and persist the access token. */
    suspend fun signInWithGoogle(idToken: String): Outcome<User> = safeCall {
        val resp = api.createSession(SessionRequest(idToken = idToken))
        resp.accessToken?.let { tokenStore.saveToken(it) }
        val user = User(resp.user.id, resp.user.email, resp.user.name, resp.user.picture)
        _currentUser.value = user
        user
    }

    /** Validate the cached token by calling /api/auth/me. */
    suspend fun refreshSession(): Outcome<User> = safeCall {
        val dto = api.me()
        val user = User(dto.id, dto.email, dto.name, dto.picture)
        _currentUser.value = user
        user
    }

    suspend fun logout(): Outcome<Unit> {
        val result = safeCall { api.logout() }
        // Always clear local state even if the server call fails — we still want to forget the user.
        tokenStore.clear()
        _currentUser.value = null
        return when (result) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> Outcome.Success(Unit) // best-effort
            Outcome.Loading -> Outcome.Loading
        }
    }
}
