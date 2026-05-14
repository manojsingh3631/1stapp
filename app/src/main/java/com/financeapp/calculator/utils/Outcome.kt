package com.financeapp.calculator.utils

/**
 * Lightweight Result type that doesn't shadow kotlin.Result.
 * Used by repositories so ViewModels can show a single error path.
 */
sealed class Outcome<out T> {
    data class Success<T>(val data: T) : Outcome<T>()
    data class Failure(val message: String, val cause: Throwable? = null) : Outcome<Nothing>()
    data object Loading : Outcome<Nothing>()

    fun getOrNull(): T? = (this as? Success)?.data
    val isSuccess get() = this is Success
    val isFailure get() = this is Failure
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Failure -> this
    Outcome.Loading -> this
}

inline fun <T> safeCall(block: () -> T): Outcome<T> = try {
    Outcome.Success(block())
} catch (t: Throwable) {
    val msg = when (t) {
        is java.net.UnknownHostException -> "No internet connection."
        is java.net.SocketTimeoutException -> "Request timed out. Please retry."
        is retrofit2.HttpException -> when (t.code()) {
            401 -> "Session expired. Please sign in again."
            403 -> "You don't have permission to do that."
            in 500..599 -> "Server error. Please try again later."
            else -> "Request failed (${t.code()})."
        }
        else -> t.message ?: "Something went wrong."
    }
    Outcome.Failure(msg, t)
}
