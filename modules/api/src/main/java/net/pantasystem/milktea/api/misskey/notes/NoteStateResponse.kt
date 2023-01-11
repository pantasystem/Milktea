package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable
data class NoteStateResponse(
    val isFavorited: Boolean,
    val isWatching: Boolean? = null,
    val isMutedThread: Boolean? = null,
)