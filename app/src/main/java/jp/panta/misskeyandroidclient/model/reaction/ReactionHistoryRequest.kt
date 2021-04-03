package jp.panta.misskeyandroidclient.model.reaction

import jp.panta.misskeyandroidclient.model.notes.Note

data class ReactionHistoryRequest(
    val type: String?,
    val noteId: Note.Id
)