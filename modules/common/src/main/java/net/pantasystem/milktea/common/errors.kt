package net.pantasystem.milktea.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Response

@Serializable
data class Error(
    val error: Body
) {
    @Serializable
    data class Body(
        val code: String,
        val message: String,
        val id: String,
        val kind: String? = null
    )
}

sealed interface ErrorType {
    data class Misskey(val error: Error) : ErrorType
    data class Raw(val body: String) : ErrorType
}



sealed class APIError(msg: String) : Exception(msg){
    abstract val error: ErrorType?
    data class ClientException(override val error: ErrorType?) : APIError("error:$error")
    data class AuthenticationException(override val error: ErrorType?) : APIError("error:$error")
    data class ForbiddenException(override val error: ErrorType?) : APIError("error:$error")
    data class IAmAIException(override val error: ErrorType?) : APIError("API Error I am AI Error:$error")
    data class InternalServerException(override val error: ErrorType?) : APIError("API Error Internal Server Error:$error")
    data class SomethingException(override val error: ErrorType?, val statusCode: Int) : APIError("API Error:$error, statusCode:$statusCode")
    data class NotFoundException(override val error: ErrorType?) : APIError("API Error Not Found:$error")
    data class ToManyRequestsException(override val error: ErrorType?) : APIError("To many requests $error")
}

val formatter = Json {
    ignoreUnknownKeys = true
}

fun<T> Response<T>.throwIfHasError(): Response<T> {
    val error = runCancellableCatching {
        if (code() in 400 .. 599) {
            this.errorBody()?.string()?.let {
                runCancellableCatching {
                    ErrorType.Misskey(formatter.decodeFromString<Error>(it))
                }.getOrNull() ?: ErrorType.Raw(it)
            }
        } else {
            null
        }
    }.getOrNull()
    throwErrorFromStatusCode(code(), error)
    return this

}


fun throwErrorFromStatusCode(code: Int, error: ErrorType? = null) {
    when(code) {
        400 -> throw APIError.ClientException(error)
        401 -> throw APIError.AuthenticationException(error)
        403 -> throw APIError.ForbiddenException(error)
        404 -> throw APIError.NotFoundException(error)
        418 -> throw APIError.IAmAIException(error)
        500 -> throw APIError.InternalServerException(error)
        429 -> throw APIError.ToManyRequestsException(error)
        else -> if (code in 400..599) {
            throw APIError.SomethingException(error, code)
        }
    }
}