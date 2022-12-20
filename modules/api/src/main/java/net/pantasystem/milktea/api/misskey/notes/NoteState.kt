package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable
data class NoteState(
    val isFavorited: Boolean,
    val isWatching: Boolean,
    val isMutedThread: Boolean = false
)