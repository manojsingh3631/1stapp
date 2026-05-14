package com.financeapp.calculator.data.api

import com.financeapp.calculator.utils.SecureTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds the Bearer token (if present) to every outbound request.
 * The backend may also issue an HTTP-only cookie; OkHttp's CookieJar handles that path,
 * but a Bearer header is the most portable option for native clients.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: SecureTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStore.getToken()
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else original
        return chain.proceed(request)
    }
}
