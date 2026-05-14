package com.financeapp.calculator.utils

import android.content.Context
import android.content.Intent
import com.financeapp.calculator.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around GoogleSignInClient.
 *
 * Usage:
 *   - call [signInIntent] and pass the returned Intent to an ActivityResultLauncher
 *   - call [idTokenFromResult] in the launcher callback to extract the Google ID token
 *   - send that ID token to the backend via AuthRepository.signInWithGoogle
 */
@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val signInIntent: Intent get() = client.signInIntent

    /** Returns the Google ID token, or null if the result didn't contain one. */
    fun idTokenFromResult(data: Intent?): String? = try {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)
        account?.idToken
    } catch (_: ApiException) {
        null
    }

    suspend fun signOut() {
        // Fire-and-forget; sign-out on the Google side is best-effort.
        runCatching { client.signOut() }
    }
}
