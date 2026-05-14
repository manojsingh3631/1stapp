package com.financeapp.calculator

import com.financeapp.calculator.data.api.FinanceApi
import com.financeapp.calculator.data.api.dto.SessionRequest
import com.financeapp.calculator.data.api.dto.SessionResponse
import com.financeapp.calculator.data.api.dto.UserDto
import com.financeapp.calculator.data.repository.AuthRepository
import com.financeapp.calculator.utils.Outcome
import com.financeapp.calculator.utils.SecureTokenStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private val api = mockk<FinanceApi>()
    private val tokenStore = mockk<SecureTokenStore>(relaxed = true)
    private val repo = AuthRepository(api, tokenStore)

    @Test
    fun signInWithGoogle_savesTokenAndExposesUser() = runTest {
        val resp = SessionResponse(
            accessToken = "tok",
            tokenType = "Bearer",
            user = UserDto("u1", "a@b.c", "Alice", null)
        )
        coEvery { api.createSession(SessionRequest("id-token")) } returns resp

        val outcome = repo.signInWithGoogle("id-token")
        assertTrue(outcome is Outcome.Success)
        assertEquals("u1", (outcome as Outcome.Success).data.id)
        verify { tokenStore.saveToken("tok") }
        assertEquals("Alice", repo.currentUser.value?.name)
    }

    @Test
    fun signInWithGoogle_propagatesFailureMessage() = runTest {
        coEvery { api.createSession(any()) } throws java.net.UnknownHostException("no DNS")
        val outcome = repo.signInWithGoogle("id-token")
        assertTrue(outcome is Outcome.Failure)
        assertTrue(
            "Should have friendly offline message",
            (outcome as Outcome.Failure).message.contains("internet", ignoreCase = true)
        )
    }

    @Test
    fun logout_alwaysClearsLocalState_evenIfServerFails() = runTest {
        coEvery { api.logout() } throws RuntimeException("boom")
        every { tokenStore.clear() } returns Unit
        val outcome = repo.logout()
        // logout swallows server errors for the user — local state must still clear.
        assertTrue(outcome is Outcome.Success)
        coVerify { tokenStore.clear() }
    }
}
