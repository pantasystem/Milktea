package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SignInRequest(
    @SerialName("username")
    val username: String,

    @SerialName("password")
    val password: String,
)
