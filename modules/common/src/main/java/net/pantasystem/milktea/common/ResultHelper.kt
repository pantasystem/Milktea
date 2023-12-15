package net.pantasystem.milktea.common

import kotlinx.coroutines.CancellationException

inline fun <T, R> T.runCancellableCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: OutOfMemoryError) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

inline fun <R> runCancellableCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: OutOfMemoryError) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

inline fun <R, T> Result<T>.mapCancellableCatching(transform: (value: T) -> R): Result<R> {
    return try {
        val result = getOrThrow()
        runCancellableCatching {
            transform(result)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: OutOfMemoryError) {
        throw e
    } catch (e: Exception) {
        return Result.failure(e)
    }
}