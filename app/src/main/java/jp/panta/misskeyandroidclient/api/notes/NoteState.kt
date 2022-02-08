package jp.panta.misskeyandroidclient.api.notes

import kotlinx.serialization.Serializable

@Serializable data class NoteState(val isFavorited: Boolean, val isWatching: Boolean)