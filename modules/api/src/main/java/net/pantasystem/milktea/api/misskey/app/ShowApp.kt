package net.pantasystem.milktea.api.misskey.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShowApp(
    @SerialName("i")
    val i: String,

    @SerialName("appId")
    val appId: String
)