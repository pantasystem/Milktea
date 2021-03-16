package jp.panta.misskeyandroidclient.model.notes.reaction

import jp.panta.misskeyandroidclient.model.notes.Note

data class CreateReaction(
    val noteId: Note.Id,
    val reaction: String
)