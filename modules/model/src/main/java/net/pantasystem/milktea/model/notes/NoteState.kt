package net.pantasystem.milktea.model.notes

data class NoteState(
    val isFavorited: Boolean,
    val isWatching: Boolean,
    val isMutedThread: Boolean?
)