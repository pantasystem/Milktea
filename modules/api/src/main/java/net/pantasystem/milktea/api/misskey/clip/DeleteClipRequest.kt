package net.pantasystem.milktea.api.misskey.clip

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class DeleteClipRequest(
    @SerialName("i")
    val i: String,

    @SerialName("clipId")
    val clipId: String,
)