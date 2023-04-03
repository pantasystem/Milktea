package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Session(
    @SerialName("token")
    val token: String,

    @SerialName("url")
    val url: String,
)
