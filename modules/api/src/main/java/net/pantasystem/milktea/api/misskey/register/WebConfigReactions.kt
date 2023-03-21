package net.pantasystem.milktea.api.misskey.register

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class WebClientRegistries(
    @SerialName("reactions")
    val reactions: List<String>
)

@kotlinx.serialization.Serializable
data class WebClientBaseRequest(
    @SerialName("i")
    val i: String,

    @SerialName("scope")
    val scope: List<String>
)