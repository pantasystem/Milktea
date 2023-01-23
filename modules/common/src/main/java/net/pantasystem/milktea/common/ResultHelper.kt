package net.pantasystem.milktea.common

import kotlinx.coroutines.CancellationException

inline fun <T, R> T.runCancellableCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

inline fun <R> runCancellableCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

inline fun <R, T> Result<T>.mapCancellableCatching(transform: (value: T) -> R): Result<R> {
    val successResult = getOrNull()
    return when {
        successResult != null -> runCancellableCatching { transform(successResult) }
        else -> Result.failure(exceptionOrNull() ?: error("Unreachable state"))
    }
}