package jp.panta.misskeyandroidclient.api

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
    data class IAmAIException(override val error: Error?) : APIError("error:$error")
    data class InternalServerException(override val error: Error?) : APIError("error:$error")
    data class SomethingException(override val error: Error?) : APIError("error:$error")
}

val formatter = Json {

}

fun<T> Response<T>.throwIfHasError(): Response<T> {
    val error = runCatching {
        this.errorBody()?.string()?.let {
            formatter.decodeFromString<Error>(it)
        }
    }.getOrNull()
    error.let {
        println("throwIfHasError: code:${this.code()}, errorBody:${error}")
        when(this.code()) {
            400 -> throw APIError.ClientException(it)
            401 -> throw APIError.AuthenticationException(it)
            403 -> throw APIError.ForbiddenException(it)
            418 -> throw APIError.IAmAIException(it)
            500 -> throw APIError.InternalServerException(it)
            else -> APIError.SomethingException(it)

        }
    }
    return this

}
