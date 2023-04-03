package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class AppSecret(
    @SerialName("appSecret")
    val appSecret: String,
)