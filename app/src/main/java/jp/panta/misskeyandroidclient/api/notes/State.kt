package jp.panta.misskeyandroidclient.api.notes

import kotlinx.serialization.Serializable

@Serializable data class State(val isFavorited: Boolean, val isWatching: Boolean)