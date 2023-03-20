package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteStateResponse(
    @SerialName("isFavorited")
    val isFavorited: Boolean,

    @SerialName("isWatching")
    val isWatching: Boolean? = null,

    @SerialName("isMutedThread")
    val isMutedThread: Boolean? = null,
)