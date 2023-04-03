package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SignInResponse(
    @SerialName("id")
    val id: String,

    @SerialName("i")
    val i: String,
)
