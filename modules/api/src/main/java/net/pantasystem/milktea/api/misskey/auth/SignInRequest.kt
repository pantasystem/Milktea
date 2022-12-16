package net.pantasystem.milktea.api.misskey.auth

@kotlinx.serialization.Serializable
data class SignInRequest(
    val username: String,
    val password: String,
)
