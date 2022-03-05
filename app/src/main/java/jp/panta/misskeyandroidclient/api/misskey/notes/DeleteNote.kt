package jp.panta.misskeyandroidclient.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable data class DeleteNote(val i: String, val noteId: String)