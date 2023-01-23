package net.pantasystem.milktea.api.misskey.auth

@kotlinx.serialization.Serializable
data class SignInResponse(
    val id: String,
    val i: String,
)
