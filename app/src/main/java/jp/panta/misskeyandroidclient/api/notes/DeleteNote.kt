package jp.panta.misskeyandroidclient.api.notes

import kotlinx.serialization.Serializable

@Serializable data class DeleteNote(val i: String, val noteId: String)