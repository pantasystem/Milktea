package net.pantasystem.milktea.api.misskey.ap

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ApResolveRequest(
    @SerialName("i")
    val i: String,

    @SerialName("uri")
    val uri: String,
)
