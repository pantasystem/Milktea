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
    data class Misskey(val error: Error) : ErrorType {
        val errorCodeeType: MisskeyErrorCodes? by lazy {
            MisskeyErrorCodes.values().find { it.code == error.error.code }
        }
    }
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

enum class MisskeyErrorCodes(
    val code: String
) {

    InternalError("INTERNAL_ERROR"),
    UserIsDeleted("USER_IS_DELETED"),
    FileRequired("FILE_REQUIRED"),
    NoSuchList("NO_SUCH_LIST"),
    YouHaveBeenBlocked("YOU_HAVE_BEEN_BLOCKED"),
    AccessDenied("ACCESS_DENIED"),
    FailedToResolveRemoteUser("FAILED_TO_RESOLVE_REMOTE_USER"),
    YourPost("YOUR_POST"),
    UnknownApiEndpoint("UNKNOWN_API_ENDPOINT"),
    NoSuchChannel("NO_SUCH_CHANNEL"),
    NoSuchPage("NO_SUCH_PAGE"),
    NoSuchNote("NO_SUCH_NOTE"),
    NoSuchUser("NO_SUCH_USER"),
    NoSuchClip("NO_SUCH_CLIP"),
    IncorrectPassword("INCORRECT_PASSWORD"),
    NoSuchKey("NO_SUCH_KEY"),
    TooManyAntennas("TOO_MANY_ANTENNAS"),
    FollowRequestNotFound("FOLLOW_REQUEST_NOT_FOUND"),
    LtlDisabled("LTL_DISABLED"),
    NoSuchFile("NO_SUCH_FILE"),
    NoSuchAntenna("NO_SUCH_ANTENNA"),
    TooManyMutedWords("TOO_MANY_MUTED_WORDS"),
    NoSuchRole("NO_SUCH_ROLE"),
    NoSuchSession("NO_SUCH_SESSION"),
    TooManyClips("TOO_MANY_CLIPS"),
    ReactionsNotPublic("REACTIONS_NOT_PUBLIC"),
    NoSuchFolder("NO_SUCH_FOLDER"),
    NoSuchReplyTarget("NO_SUCH_REPLY_TARGET"),
    NoSuchEmoji("NO_SUCH_EMOJI"),
    Forbidden("FORBIDDEN"),
    TooManyClipNotes("TOO_MANY_CLIP_NOTES"),
    TooManyUsers("TOO_MANY_USERS"),
    PinLimitExceeded("PIN_LIMIT_EXCEEDED"),
    NoSuchUserList("NO_SUCH_USER_LIST"),
    NoSuchApp("NO_SUCH_APP"),
    YourPage("YOUR_PAGE"),
    YourFlash("YOUR_FLASH"),
    NoFreeSpace("NO_FREE_SPACE"),
    RolePermissionDenied("ROLE_PERMISSION_DENIED"),
    YourAccountMoved("YOUR_ACCOUNT_MOVED"),
    YourAccountSuspended("YOUR_ACCOUNT_SUSPENDED"),
    CredentialRequired("CREDENTIAL_REQUIRED"),
    PermissionDenied("PERMISSION_DENIED"),
}