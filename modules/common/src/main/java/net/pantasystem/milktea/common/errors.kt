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



sealed class APIError(msg: String) : Exception(msg){
    abstract val error: Error?
    data class ClientException(override val error: Error?) : APIError("error:$error")
    data class AuthenticationException(override val error: Error?) : APIError("error:$error")
    data class ForbiddenException(override val error: Error?) : APIError("error:$error")
    data class IAmAIException(override val error: Error?) : APIError("API Error I am AI Error:$error")
    data class InternalServerException(override val error: Error?) : APIError("API Error Internal Server Error:$error")
    data class SomethingException(override val error: Error?, val statusCode: Int) : APIError("API Error:$error, statusCode:$statusCode")
    data class NotFoundException(override val error: Error?) : APIError("API Error Not Found:$error")
    data class ToManyRequestsException(override val error: Error?) : APIError("To many requests $error")
}

val formatter = Json

fun<T> Response<T>.throwIfHasError(): Response<T> {
    val error = runCancellableCatching {
        this.errorBody()?.string()?.let {
            formatter.decodeFromString<Error>(it)
        }
    }.getOrNull()
    throwErrorFromStatusCode(code(), error)
    return this

}


fun throwErrorFromStatusCode(code: Int, error: Error? = null) {
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