package jp.panta.misskeyandroidclient.model.reaction

import jp.panta.misskeyandroidclient.model.notes.Note

data class ReactionHistoryRequest(
    val noteId: Note.Id,
    val type: String?,
)