package jp.panta.misskeyandroidclient.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable data class CreateReaction (val i: String, val noteId: String, val reaction: String)