package net.pantasystem.milktea.data.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable data class NoteState(val isFavorited: Boolean, val isWatching: Boolean)